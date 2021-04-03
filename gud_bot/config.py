import os
import pathlib
import typing

import yaml

config_directory = pathlib.Path(
    os.environ.get("GUD_BOT_CONFIG_DIR", pathlib.Path(__file__).resolve().parent.parent)).resolve()


class Config(typing.TypedDict):
    database: str
    extensions: typing.List[str]
    token: str
    presence: str


def load_config() -> Config:
    with open(config_directory / (
            "config.yaml" if os.path.isfile(config_directory / "config.yaml") else "config.yaml.heroku"), "r",
              encoding="utf-8") as fp:
        config: Config = yaml.unsafe_load(fp)

    with open(config_directory / "config.yaml", "w", encoding="utf-8") as fp:
        yaml.dump(config, fp, default_flow_style=False, indent=2)
    return config


config = load_config()
del load_config
