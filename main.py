import discord
from discord.ext import commands
import yaml
import os

with open("config.yaml" if os.path.isfile("config.yaml") else "config.yaml.heroku", "r", encoding="utf-8") as fp:
    config = yaml.unsafe_load(fp)

with open("config.yaml", "w", encoding="utf-8") as fp:
    yaml.dump(config, fp, default_flow_style=False)

bot = commands.Bot(command_prefix=["$ ", "*", "$"], description="Gud Bot", case_insensitive=True)
bot.config = config
bot.load_extension("m6rr")
bot.load_extension("jishaku")
bot.load_extension("roles")


@bot.event
async def on_ready():
    print(
        f'\n\nLogged in as: {bot.user.name} - {bot.user.id}\nVersion: {discord.__version__}\n')
    await bot.change_presence(activity=discord.Game(name="$ help"))
    print('Successfully set rich presence')

if os.path.isfile("bot_token.txt"):
    with open("bot_token.txt", "r") as fp:
        token = fp.read().strip()
else:
    token = os.environ["BOT_TOKEN"]
bot.run(token)
