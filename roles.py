import discord
from discord.ext import commands
from kb1000_discordpy_common import force_async
import functools
import typing


@functools.lru_cache(maxsize=None)
def in_guild(id_: int) -> typing.Callable[[commands.Command], commands.Command]:
    # this decorator turns this into a decorator itself
    @commands.check
    def actual_check(ctx: commands.Context) -> bool:
        return ctx.guild.id == id_

    return actual_check


class Roles(commands.Cog):
    bot: commands.Bot
    guild: discord.Guild

    def __init__(self, bot: commands.Bot):
        self.bot = bot
        bot.loop.create_task(self.init())

    @force_async
    async def init(self):
        await self.bot.wait_until_ready()
        self.guild = self.bot.get_guild(560151801471565885)

    def _make_role_command(name: str, snowflake: int, cmd_name: typing.Optional[str] = None):
        async def command(self, ctx):
            role = self.guild.get_role(snowflake)
            if role not in ctx.author.roles:
                await ctx.author.add_roles(role)
            else:
                await ctx.author.remove_roles(role)
            if ctx.message is not None:
                await ctx.message.add_reaction("\u2705")

        command.__name__ = name

        if cmd_name:
            command = commands.command(name=cmd_name)(command)
        else:
            command = commands.command()(command)

        return in_guild(560151801471565885)(command)

    java = _make_role_command("java", 560154541773946930)
    c_sharp = _make_role_command("c#", 560154254493614080)
    python = _make_role_command("python", 581870128908730381)
    cplusplus = _make_role_command("cplusplus", 560154750046175236, "c++")
    c = _make_role_command("c", 584379452071608340)
    css = _make_role_command("css", 584380053500985382)
    html = _make_role_command("html", 584379964850307072)
    php = _make_role_command("php", 584379817219063818)
    javascript = _make_role_command("javascript", 592386652597649448)


def setup(bot):
    bot.add_cog(Roles(bot))
