from discord.ext import commands
from kb1000_discordpy_common import force_async

class Roles(commands.Cog):
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

    @commands.command(name="c#")
    @force_async
    async def c_sharp(self, ctx):
        if ctx.guild != self.guild:
            return
        await ctx.author.add_roles(self.guild.get_role(560154254493614080))
        if ctx.message is not None:
            await ctx.message.add_reaction("\u2705")

    @commands.command()
    @force_async
    async def python(self, ctx):
        if ctx.guild != self.guild:
            return
        await ctx.author.add_roles(self.guild.get_role(581870128908730381))
        if ctx.message is not None:
            await ctx.message.add_reaction("\u2705")

    @commands.command()
    @force_async
    async def web(self, ctx):
        if ctx.guild != self.guild:
            return
        await ctx.author.add_roles(self.guild.get_role(560156356770791444))
        if ctx.message is not None:
            await ctx.message.add_reaction("\u2705")

    @commands.command(name="c++")
    @force_async
    async def cplusplus(self, ctx):
        if ctx.guild != self.guild:
            return
        await ctx.author.add_roles(self.guild.get_role(560154750046175236))
        if ctx.message is not None:
            await ctx.message.add_reaction("\u2705")

def setup(bot):
    bot.add_cog(Roles(bot))
