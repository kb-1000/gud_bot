# -*- coding: utf-8 -*-
import discord
from asgiref.sync import sync_to_async
from discord.ext import commands

from .models import Tag


class Tags(commands.Cog, name="Tags"):
    """The description for Tags goes here."""
    bot: commands.Bot

    def __init__(self, bot: commands.Bot):
        self.bot = bot

    @sync_to_async
    def get_tag_content(self, guild_id: int, name: str):
        return Tag.objects.get(guild_id=guild_id, name=name).content

    @commands.group(invoke_without_command=True)
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
            await ctx.send("Tag not found.")
            return
        await ctx.send(content, reference=reference, mention_author=True,
                       allowed_mentions=discord.AllowedMentions.none())

    @tag.command("create")
    async def tag_create(self, ctx: commands.Context, name: str, *, content: str):
        pass


def setup(bot: commands.Bot):
    bot.add_cog(Tags(bot))
