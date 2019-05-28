def select_constraints_by_guild(guild):
    return 'SELECT "m6rr_constraint"."id", "m6rr_constraint"."guild", "m6rr_constraint"."maxlevel", "m6rr_constraint"."minlevel", "m6rr_constraint"."role" FROM "m6rr_constraint" WHERE "m6rr_constraint"."guild" = $1', guild
