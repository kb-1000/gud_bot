import os
import pathlib
import typing

import discord
import django
import yaml
from discord.ext import commands

config_directory = pathlib.Path(
    os.environ.get("GUD_BOT_CONFIG_DIR", pathlib.Path(__file__).absolute().parent.parent)).absolute()


class Config(typing.TypedDict):
    database: str
    extensions: typing.List[str]
    token: str
    presence: str


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
    intents = discord.Intents(members=True)

    with open(config_directory / ("config.yaml" if os.path.isfile("config.yaml") else "config.yaml.heroku"), "r",
              encoding="utf-8") as fp:
        config: Config = yaml.unsafe_load(fp)

    with open(config_directory / "config.yaml", "w", encoding="utf-8") as fp:
        yaml.dump(config, fp, default_flow_style=False, indent=2)

    bot = GudBot(command_prefix=["$ ", "*", "$", "?"],
                 description="Gud Bot", case_insensitive=True,
                 intents=intents, config=config)
    bot.load_extension("jishaku")
    for extension in config["extensions"]:
        bot.load_extension(extension)
    return bot
