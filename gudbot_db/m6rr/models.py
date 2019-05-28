from django.db import models


class Constraint(models.Model):
    guild = models.BigIntegerField()
    maxlevel = models.IntegerField(blank=True, null=True)
    minlevel = models.IntegerField(blank=True, null=True)
    role = models.BigIntegerField()
