# Generated by Django 3.1.7 on 2021-04-03 11:04

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Tag',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('guild_id', models.BigIntegerField()),
                ('name', models.CharField(max_length=100)),
                ('creator', models.BigIntegerField()),
                ('creation_time', models.DateTimeField()),
                ('content', models.TextField()),
            ],
        ),
        migrations.CreateModel(
            name='TrustedRole',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('guild_id', models.BigIntegerField()),
                ('role_id', models.BigIntegerField()),
            ],
        ),
    ]
