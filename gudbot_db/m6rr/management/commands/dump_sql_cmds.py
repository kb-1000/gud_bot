from django.conf import settings
from django.core.management.base import BaseCommand
from m6rr.models import Constraint
import pathlib
import re

_FIND_PARAMS = re.compile("%s")

def paramcounter():
    i = 0
    def func(*a, **kw):
        nonlocal i
        i += 1
        return f"${i}"
    return func

class Command(BaseCommand):
    def handle(self, *args, **options):
        qs = Constraint.objects.filter(guild=42)
        sql_cmd, sql_params = qs.query.get_compiler(qs.db).as_sql()
        sql_cmd = _FIND_PARAMS.sub(paramcounter(), sql_cmd)
        if sql_params != (42,):
            raise Exception
        with open(pathlib.Path(settings.BASE_DIR).parent / "db.py", "w") as fp:
            fp.write(f"""\
def select_constraints_by_guild(guild):
    return {repr(sql_cmd)}, guild
""")
