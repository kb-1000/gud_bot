# -*- coding: utf-8 -*-

import aiohttp
import asyncio
from discord.ext import commands
import discord


class M6RR(commands.Cog, name="M6RR"):
    def __init__(self, bot):
        self.bot = bot
        self.task = bot.loop.create_task(self.update())

    async def update(self):
        while true:
            for guild in self.bot.guilds:
                await self.update_guild(guild)
            await asyncio.sleep(120)

    # async because it may get more complicated

    async def string_from_snowflake(snowflake):
        return str(snowflake)

    async def load_leaderboard(server_snowflake):
        async with aiohttp.ClientSession() as session:
            for i in range(1000):  # no servers with >1m members
                async with session.get(f"https://mee6.xyz/api/plugins/levels/leaderboard{await string_from_snowflake(server_snowflake)}", params={"page": i, "limit": "1000"}) as response:
                    await print(response.json())

    async def update_guild(self, guild):
        await load_leaderboard(guild)


def setup(bot):
    bot.add_cog(M6RR(bot))
