from dicord.ext import commands
from kb1000_discordpy_common import force_async

class Roles(commands.Cogs):
    def __init__(self, bot):
        self.bot = bot
        bot.loop.create_task(self.init())

    @force_async
    async def init(self):
        await self.bot.wait_until_ready()
        self.guild = self.bot.get_guild(560151801471565885)

    @commands.command()
    @force_async
    async def java(self, ctx):
        if ctx.guild != self.guild:
            return
        await ctx.author.add_roles(self.guild.get_role(560154541773946930))
        if ctx.message is not None:
            await ctx.message.add_reaction("\u2705")
