from django.db import models


class Tag(models.Model):
    guild_id = models.BigIntegerField()
    name = models.CharField(max_length=100)

    creator = models.BigIntegerField()
    creation_time = models.DateTimeField(auto_now_add=True, blank=True)
    content = models.TextField()

    def get_natural_key(self):
        return self.guild_id, self.name

    def __str__(self):
        return f"{self.name} from {self.guild_id}"

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["guild_id", "name"], name="unique_tag"),
        ]


class TrustedRole(models.Model):
    guild_id = models.BigIntegerField()
    role_id = models.BigIntegerField()

    def get_natural_key(self):
        return self.guild_id, self.role_id

    def __str__(self):
        return f"{self.role_id} from {self.guild_id}"

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["guild_id", "role_id"], name="unique_trusted_role")
        ]


class TrustedUser(models.Model):
    guild_id = models.BigIntegerField()
    user_id = models.BigIntegerField()

    def get_natural_key(self):
        return self.guild_id, self.user_id

    def __str__(self):
        return f"{self.user_id} from {self.guild_id}"

    class Meta:
        constraints = [
            models.UniqueConstraint(fields=["guild_id", "user_id"], name="unique_trusted_user")
        ]
