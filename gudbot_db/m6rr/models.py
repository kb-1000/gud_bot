from django.db import models


class Constraints(models.Model):
    guild = models.IntegerField()
    maxlevel = models.IntegerField(blank=True, null=True)
    minlevel = models.IntegerField(blank=True, null=True)
    rank = models.IntegerField()
