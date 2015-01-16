# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='City',
            fields=[
                ('id', models.AutoField(primary_key=True, serialize=False)),
                ('name', models.TextField(blank=True)),
                ('state', models.TextField(blank=True)),
            ],
            options={
                'managed': False,
                'db_table': 'city',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Club',
            fields=[
                ('id', models.AutoField(primary_key=True, serialize=False)),
                ('name', models.TextField(unique=True)),
            ],
            options={
                'managed': False,
                'db_table': 'club',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Coach',
            fields=[
                ('id', models.AutoField(primary_key=True, serialize=False)),
                ('name', models.TextField(unique=True)),
            ],
            options={
                'managed': False,
                'db_table': 'coach',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Discipline',
            fields=[
                ('id', models.AutoField(primary_key=True, serialize=False)),
                ('name', models.TextField(unique=True)),
                ('shortname', models.TextField(unique=True)),
            ],
            options={
                'managed': False,
                'db_table': 'discipline',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Language',
            fields=[
                ('id', models.AutoField(primary_key=True, serialize=False)),
                ('language', models.TextField(unique=True)),
            ],
            options={
                'managed': False,
                'db_table': 'language',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Nationality',
            fields=[
                ('id', models.AutoField(primary_key=True, serialize=False)),
                ('nationality', models.TextField(unique=True)),
                ('countrycode', models.TextField(unique=True)),
            ],
            options={
                'managed': False,
                'db_table': 'nationality',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Player',
            fields=[
                ('id', models.TextField(primary_key=True, serialize=False)),
                ('firstname', models.TextField(blank=True)),
                ('name', models.TextField(blank=True)),
                ('birthdate', models.DateField(blank=True, null=True)),
                ('gender', models.CharField(max_length=1, choices=[(None, '---------'), ('m', 'm'), ('f', 'f'), ('u', 'u')], default='u')),
                ('debut_year', models.IntegerField(blank=True, null=True)),
                ('hand', models.CharField(max_length=10, choices=[(None, '---------'), ('left', 'left'), ('right', 'right'), ('unknown', 'unknown')])),
                ('height', models.FloatField(blank=True, null=True)),
                ('nickname', models.TextField(blank=True)),
                ('start_competitive', models.IntegerField(blank=True, null=True)),
                ('teammember_since', models.IntegerField(blank=True, null=True)),
            ],
            options={
                'managed': False,
                'db_table': 'player',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Image',
            fields=[
                ('player', models.ForeignKey(primary_key=True, to='analytics.Player')),
                ('url', models.TextField(primary_key=True, serialize=False)),
            ],
            options={
                'managed': False,
                'db_table': 'image',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='PlayerDiscipline',
            fields=[
                ('player', models.ForeignKey(primary_key=True, to='analytics.Player')),
                ('discipline', models.ForeignKey(primary_key=True, to='analytics.Discipline', serialize=False)),
            ],
            options={
                'managed': False,
                'db_table': 'player_discipline',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='PlayerLanguage',
            fields=[
                ('player', models.ForeignKey(primary_key=True, to='analytics.Player')),
                ('language', models.ForeignKey(primary_key=True, to='analytics.Language', serialize=False)),
            ],
            options={
                'managed': False,
                'db_table': 'player_language',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Style',
            fields=[
                ('id', models.AutoField(primary_key=True, serialize=False)),
                ('style', models.TextField(unique=True)),
            ],
            options={
                'managed': False,
                'db_table': 'style',
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Webresource',
            fields=[
                ('player', models.ForeignKey(primary_key=True, to='analytics.Player', serialize=False)),
                ('facebook', models.TextField(blank=True)),
                ('twitter', models.TextField(blank=True)),
                ('website', models.TextField(blank=True)),
            ],
            options={
                'managed': False,
                'db_table': 'webresource',
            },
            bases=(models.Model,),
        ),
    ]
