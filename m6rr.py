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
        await self.bot.wait_until_ready()
        while True:
            for guild in self.bot.guilds:
                await self.update_guild(guild.id)
            await asyncio.sleep(120)

    # async because it may get more complicated

    async def string_from_snowflake(self, snowflake):
        return str(snowflake)

    async def load_leaderboard(self, server_snowflake):
        async with aiohttp.ClientSession() as session:
            for i in range(1000):  # no servers with >1m members
                async with session.get(f"https://mee6.xyz/api/plugins/levels/leaderboard/{await self.string_from_snowflake(server_snowflake)}", params={"page": i, "limit": "999"}) as response:
                    resp = (await response.json())["players"]
                    if not resp:
                        break
            else:
                print("server too big: " + server_snowflake)

    async def update_guild(self, guild):
        await self.load_leaderboard(guild)


def setup(bot):
    bot.add_cog(M6RR(bot))
