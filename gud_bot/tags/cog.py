# -*- coding: utf-8 -*-
import typing

import discord
from asgiref.sync import sync_to_async
from discord.ext import commands
from django.db import IntegrityError

from .models import Tag, TrustedUser, TrustedRole


class NotTrustedError(commands.CheckFailure):
    def __init__(self):
        super().__init__('You are not on the trust list for this command.')


@sync_to_async
def is_trusted_predicate(ctx: commands.Context):
    # TODO: check TrustedUser and TrustedRole
    if TrustedUser.objects.filter(guild_id=ctx.guild.id, user_id=ctx.author.id):
        return True
    if TrustedRole.objects.filter(guild_id=ctx.guild.id, role_id__in=[x.id for x in ctx.author.roles]):
        return True
    raise NotTrustedError()


def is_trusted():
    async def predicate(ctx: commands.Context):
        return await is_trusted_predicate(ctx)

    return commands.check(predicate)


class Tags(commands.Cog, name="Tags"):
    """The description for Tags goes here."""
    bot: commands.Bot

    def __init__(self, bot: commands.Bot):
        self.bot = bot

    @sync_to_async
    def get_tag_content(self, guild_id: int, name: str) -> str:
        return Tag.objects.get(guild_id=guild_id, name=name).content

    @sync_to_async
    def create_tag(self, guild_id: int, creator: int, name: str, content: str):
        Tag.objects.create(guild_id=guild_id, creator=creator, name=name, content=content)

    @sync_to_async
    def edit_tag(self, guild_id: int, creator: int, name: str, content: str, has_admin: bool):
        if has_admin:
            tag = Tag.objects.get(guild_id=guild_id, name=name)
        else:
            tag = Tag.objects.get(guild_id=guild_id, creator=creator, name=name)
        tag.content = content
        tag.save(force_update=True)

    @sync_to_async
    def remove_tag(self, guild_id: int, creator: int, name: str, has_admin: bool):
        if has_admin:
            tag = Tag.objects.get(guild_id=guild_id, name=name)
        else:
            tag = Tag.objects.get(guild_id=guild_id, creator=creator, name=name)
        tag.delete()

    @sync_to_async
    def trust_user(self, guild_id: int, user_id: int):
        TrustedUser.objects.create(guild_id=guild_id, user_id=user_id)

    @sync_to_async
    def trust_role(self, guild_id: int, role_id: int):
        TrustedRole.objects.create(guild_id=guild_id, role_id=role_id)

    @sync_to_async
    def untrust_user(self, guild_id: int, user_id: int):
        TrustedUser.objects.get(guild_id=guild_id, user_id=user_id).delete()

    @sync_to_async
    def untrust_role(self, guild_id: int, role_id: int):
        TrustedRole.objects.get(guild_id=guild_id, role_id=role_id).delete()

    @sync_to_async
    def list_tags(self, guild_id: int) -> typing.List[Tag]:
        return list(Tag.objects.filter(guild_id=guild_id))

    @commands.group(invoke_without_command=True)
    @commands.guild_only()
    async def tag(self, ctx: commands.Context, name: str):
        message: discord.Message = ctx.message
        reference = None
        if message.reference:
            reference = discord.MessageReference(message_id=message.reference.message_id,
                                                 channel_id=message.reference.channel_id,
                                                 guild_id=message.reference.guild_id, fail_if_not_exists=False)
        try:
            content = await self.get_tag_content(ctx.guild.id, name)
        except (Tag.DoesNotExist, Tag.MultipleObjectsReturned):
            await ctx.send("\N{CROSS MARK} Tag not found.")
            return
        await ctx.send(content, reference=reference, mention_author=True,
                       allowed_mentions=discord.AllowedMentions.none())

    @tag.command("create")
    @commands.guild_only()
    @commands.check_any(commands.is_owner(), commands.has_guild_permissions(administrator=True), is_trusted())
    async def tag_create(self, ctx: commands.Context, name: str, *, content: str):
        if len(name) > 100:
            await ctx.send("\N{CROSS MARK} Name too long.")
        try:
            await self.create_tag(ctx.guild.id, ctx.author.id, name, content)
        except IntegrityError:
            await ctx.send(f"\N{CROSS MARK} Error: tag \"{name}\" already exists",
                           allowed_mentions=discord.AllowedMentions.none())
            return
        await ctx.send(f"\N{WHITE HEAVY CHECK MARK} Successfully created tag \"{name}\"",
                       allowed_mentions=discord.AllowedMentions.none())

    @tag.command("edit")
    @commands.guild_only()
    @commands.check_any(commands.is_owner(), commands.has_guild_permissions(administrator=True), is_trusted())
    async def tag_edit(self, ctx: commands.Context, name: str, *, content: str):
        try:
            await self.edit_tag(ctx.guild.id, ctx.author.id, name, content,
                                ctx.author.guild_permissions.administrator or await ctx.bot.is_owner(ctx.author))
        except (Tag.DoesNotExist, Tag.MultipleObjectsReturned):
            await ctx.send(f"\N{CROSS MARK} Tag does not exist, or you're not the owner of the tag.",
                           allowed_mentions=discord.AllowedMentions.none())
            return
        await ctx.send(f"\N{WHITE HEAVY CHECK MARK} Tag \"{name}\" edited successfully.",
                       allowed_mentions=discord.AllowedMentions.none())

    @tag.command("remove", aliases=["delete"])
    @commands.guild_only()
    @commands.check_any(commands.is_owner(), commands.has_guild_permissions(administrator=True), is_trusted())
    async def tag_remove(self, ctx: commands.Context, name: str):
        try:
            await self.remove_tag(ctx.guild.id, ctx.author.id, name,
                                  ctx.author.guild_permissions.administrator or await ctx.bot.is_owner(ctx.author))
        except (Tag.DoesNotExist, Tag.MultipleObjectsReturned):
            await ctx.send(f"\N{CROSS MARK} Tag does not exist, or you're not the owner of the tag.",
                           allowed_mentions=discord.AllowedMentions.none())
            return
        await ctx.send(f"\N{WHITE HEAVY CHECK MARK} Tag \"{name}\" removed successfully.",
                       allowed_mentions=discord.AllowedMentions.none())

    @tag.command("list")
    @commands.guild_only()
    async def tag_list(self, ctx: commands.Context):
        tags = await self.list_tags(ctx.guild.id)
        tags_string = "\n".join(tag.name for tag in tags)
        embed = discord.Embed(description=tags_string)
        embed.set_author(name=ctx.guild.name, icon_url=ctx.guild.icon_url)
        await ctx.send(embed=embed, allowed_mentions=discord.AllowedMentions.none())

    @tag.command("trust")
    @commands.guild_only()
    @commands.check_any(commands.is_owner(), commands.has_guild_permissions(administrator=True))
    async def tag_trust(self, ctx: commands.Context, trusted_object: typing.Union[discord.Member, discord.Role]):
        try:
            if isinstance(trusted_object, discord.Member):
                await self.trust_user(ctx.guild.id, trusted_object.id)
            elif isinstance(trusted_object, discord.Role):
                await self.trust_role(ctx.guild.id, trusted_object.id)
        except IntegrityError:
            await ctx.send(
                f"\N{CROSS MARK} {trusted_object.mention} ({trusted_object.name}) is already on the trust list.",
                allowed_mentions=discord.AllowedMentions.none())
            return
        await ctx.send(
            f"\N{WHITE HEAVY CHECK MARK} Successfully added {trusted_object.mention} ({trusted_object.name}) to the trust list.",
            allowed_mentions=discord.AllowedMentions.none())

    @tag.command("untrust")
    @commands.guild_only()
    @commands.check_any(commands.is_owner(), commands.has_guild_permissions(administrator=True))
    async def tag_untrust(self, ctx: commands.Context, trusted_object: typing.Union[discord.Member, discord.Role]):
        try:
            if isinstance(trusted_object, discord.Member):
                await self.untrust_user(ctx.guild.id, trusted_object.id)
            elif isinstance(trusted_object, discord.Role):
                await self.untrust_role(ctx.guild.id, trusted_object.id)
        except (TrustedRole.DoesNotExist, TrustedRole.MultipleObjectsReturned, TrustedUser.DoesNotExist,
                TrustedUser.MultipleObjectsReturned):
            await ctx.send(
                f"\N{CROSS MARK} {trusted_object.mention} ({trusted_object.name}) is not on the trust list.",
                allowed_mentions=discord.AllowedMentions.none())
            return
        await ctx.send(
            f"\N{WHITE HEAVY CHECK MARK} Successfully removed {trusted_object.mention} ({trusted_object.name}) from the trust list.",
            allowed_mentions=discord.AllowedMentions.none())


def setup(bot: commands.Bot):
    bot.add_cog(Tags(bot))
