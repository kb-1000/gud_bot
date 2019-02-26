import discord
from discord.ext import commands

bot = commands.Bot(command_prefix="$ ", description="Gud Bot")
bot.load_extension("m6rr")
bot.load_extension("jishaku")

@bot.event
async def on_ready():
    print(f'\n\nLogged in as: {bot.user.name} - {bot.user.id}\nVersion: {discord.__version__}\n')
    await bot.change_presence(activity=discord.Game(name="Gud Bot"))
    print('Successfully set rich presence')

with open("bot_token.txt", "r") as fp:
    token = fp.read().strip()
bot.run(token)
