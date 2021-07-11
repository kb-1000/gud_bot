import itertools
import pathlib
import sys

if __name__ == "__main__":
    import uvloop
    import asyncio
    asyncio.set_event_loop_policy(uvloop.EventLoopPolicy())
    bot_module_dir = pathlib.Path(__file__).absolute().parent
    bot_dir = bot_module_dir.parent

    for i, path_entry in zip(itertools.count(), sys.path):
        try:
            if pathlib.Path(path_entry).samefile(bot_module_dir):
                sys.path[i] = str(bot_dir)
        except FileNotFoundError:
            # can't be the same file if it doesn't exist
            pass

    from gud_bot.bot import make_bot

    make_bot().run()
