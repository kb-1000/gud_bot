# -*- coding: utf-8 -*-

import aiohttp
import asyncio
from discord.ext import commands
import itertools
import discord
import asyncpg
import db
import yaml


class M6RR(commands.Cog, name="M6RR"):
    def __init__(self, bot):
        self.bot = bot
        self.task = bot.loop.create_task(self.update())

    async def update(self):
        self.db = await asyncpg.connect(self.bot.config["database"])
        await self.bot.wait_until_ready()
        mee6 = self.bot.get_user(159985870458322944)
        while True:
            for guild in (guild for guild in self.bot.guilds
                          if mee6 in guild.members):
                if guild.members:
                    await self.update_guild(guild)
            await asyncio.sleep(120)

    # async because it may get more complicated
    async def string_from_object(self, object):
        return str(object.id)

    async def load_leaderboard(self, guild):
        async with aiohttp.ClientSession() as session:
            whole = []
            for i in range(1000):  # no servers with >1m members
                async with session.get(f"https://mee6.xyz/api/plugins/levels/leaderboard/{await self.string_from_object(guild)}", params={"page": i, "limit": "999"}) as response:
                    resp = (await response.json())["players"]
                    if not resp:
                        break
                    whole.append(resp)
            else:
                print("server too big: " + server_snowflake)
            return list(itertools.chain(*whole))

    async def update_guild(self, guild):
        await self.apply(guild, await self.load_leaderboard(guild))

    async def apply(self, guild, data):
        constraints = await self.db.fetch(*db.select_constraints_by_guild(guild.id))
        for user in data:
            level: int = user["level"]
            for constraint in constraints:
                state = True
                minlevel = constraint["minlevel"]
                maxlevel = constraint["maxlevel"]
                if minlevel is not None and level < minlevel:
                    state = False
                elif maxlevel is not None and level > maxlevel:
                    state = False
                bot_user = guild.get_member(int(user["id"]))
                if state and guild.get_role(
                        constraint["role"]) not in bot_user.roles:
                    await bot_user.add_roles(guild.get_role(constraint["role"]))


def setup(bot):
    bot.add_cog(M6RR(bot))
