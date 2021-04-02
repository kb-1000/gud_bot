# -*- coding: utf-8 -*-

from asgiref.sync import sync_to_async
from discord.ext import commands
from .models import Tag


class Tags(commands.Cog, name="Tags"):
    """The description for Tags goes here."""
    bot: commands.Bot

    def __init__(self, bot: commands.Bot):
        self.bot = bot

    @sync_to_async
    def get_tag(self, guild_id: int, name: str):
        return Tag.objects.get(guild=guild_id, name=name)

    @commands.group(invoke_without_command=True)
    async def tag(self, ctx: commands.Context, name: str):
        pass

    @tag.command("create")
    async def tag_create(self, ctx: commands.Context, name: str, *, content: str):
        pass


def setup(bot: commands.Bot):
    bot.add_cog(Tags(bot))
