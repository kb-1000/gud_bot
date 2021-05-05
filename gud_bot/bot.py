import os

import discord
import django
from discord.ext import commands

from .config import Config, config


class GudBot(commands.Bot):
    config: Config

    def __init__(self, *, config: Config, **options):
        super().__init__(**options)
        self.config = config

    async def start(self, *args, **kwargs):
        return await super().start(self.config["token"], *args, **kwargs)

    async def on_ready(self):
        print(
            f'\n\nLogged in as: {self.user.name} - {self.user.id}\nVersion: {discord.__version__}\n')
        await self.change_presence(activity=discord.Game(name="$ help (made by kb1000#3709!)"))
        print('Successfully set rich presence')


def make_bot() -> GudBot:
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'gud_bot.project.settings')
    django.setup()
    intents = discord.Intents.default()
    intents.members = True

    bot = GudBot(command_prefix=commands.when_mentioned_or(["$ ", "*", "$", "?"]),
                 description="Gud Bot", case_insensitive=True,
                 intents=intents, config=config)
    bot.load_extension("jishaku")
    for extension in config["extensions"]:
        bot.load_extension(extension)
    return bot
