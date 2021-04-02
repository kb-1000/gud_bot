from django.db import models


class Tag(models.Model):
    guild = models.BigIntegerField()
    name = models.CharField(max_length=100)

    creator = models.BigIntegerField()
    creation_time = models.DateTimeField()
    content = models.TextField()

    def get_natural_key(self):
        return self.guild, self.name
