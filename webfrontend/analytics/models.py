from __future__ import unicode_literals
from django.db import models
import datetime

class Club(models.Model):
    id   = models.AutoField(primary_key=True)
    name = models.TextField(unique=True)
    
    class Meta:
        managed  = False
        db_table = 'club'
    
    def __str__(self):
        return "%s" % self.name


class City(models.Model):
    id    = models.AutoField(primary_key=True)
    name  = models.TextField(blank=True)
    state = models.TextField(blank=True)

    class Meta:
        managed  = False
        db_table = 'city'

    def __str__(self):
        return "%s, %s" % (self.state, self.name)


class Coach(models.Model):
    id   = models.AutoField(primary_key=True)
    name = models.TextField(unique=True)

    class Meta:
        managed  = False
        db_table = 'coach'

    def __str__(self):
        return "%s" % self.name


class Language(models.Model):
    id       = models.AutoField(primary_key=True)
    language = models.TextField(unique=True)

    class Meta:
        managed  = False
        db_table = 'language'

    def __str__(self):
        return "%s" % self.language


class Nationality(models.Model):
    id          = models.AutoField(primary_key=True) 
    nationality = models.TextField(unique=True)

    class Meta:
        managed  = False
        db_table = 'nationality'

    def __str__(self):
        return "%s" % self.nationality


class Style(models.Model):
    id    = models.AutoField(primary_key=True)
    style = models.TextField(unique=True)

    class Meta:
        managed  = False
        db_table = 'style'

    def __str__(self):
        return "%s" % self.style


class Player(models.Model):
    MALE        = 'm'
    FEMALE      = 'f'
    UNSPECIFIED = 'u'
    LEFT        = 'left'
    RIGHT       = 'right'
    UNKNOWN     = 'unknown'
    GENDER_CHOICES = (
        (MALE,        'm') 
        , (FEMALE,      'f') 
        , (UNSPECIFIED, 'u')
    )
    HAND_CHOICES   = (
        (LEFT,    'left')
        , (RIGHT,   'right')
        , (UNKNOWN, 'unknown')
    )
    HAND_CHOICES_AND_EMPTY = (
        ('',      '---------')
        , (LEFT,    'left')
        , (RIGHT,   'right')
        , (UNKNOWN, 'unknown')
    )

    id                 = models.TextField(primary_key=True)
    firstname          = models.TextField(blank=True)
    name               = models.TextField(blank=True)
    birthdate          = models.DateField(blank=True, null=True)
    gender             = models.CharField(max_length = 1, 
                                          choices = GENDER_CHOICES, 
                                          default = UNSPECIFIED)
    birthplace_city    = models.ForeignKey(City, 
                                           related_name='player_birthplace_city', 
                                           blank=True, 
                                           null=True)
    club               = models.ForeignKey(Club, blank=True, null=True)
    coach              = models.ForeignKey(Coach, blank=True, null=True)
    cur_residence_city = models.ForeignKey(City, 
                                           related_name='player_cur_residence_city',
                                           blank=True, 
                                           null=True)
    debut_year         = models.IntegerField(blank=True, null=True)
    hand               = models.CharField(max_length = 10,
                                          choices = HAND_CHOICES)
    height             = models.FloatField(blank=True, null=True)
    nationality        = models.ForeignKey(Nationality, blank=True, null=True)
    nickname           = models.TextField(blank=True)
    start_competitive  = models.IntegerField(blank=True, null=True)
    style              = models.ForeignKey(Style, blank=True, null=True)
    teammember_since   = models.IntegerField(blank=True, null=True)

    class Meta:
        managed  = False
        db_table = 'player'

    # def clean(self):
    #     if self.birthdate > datetime.date.today():
    #         raise ValidationError('Birthdate is in future!')
    #     if self.height < 50 or self.height > 250:
    #         raise ValidationError('Invalide height value!')

    def __str__(self):
        if self.firstname and self.name:
            return "%s, %s" % (self.firstname, self.name)
        else:
            return "%s" % self.id

    # def birthdate_realistic(self):
    #     if self.birthdate <= datetime.date.today():
    #         return True
    #     else:
    #         return False


class PlayerLanguage(models.Model):
    player   = models.ForeignKey(Player, primary_key=True)
    language = models.ForeignKey(Language, primary_key=True)

    class Meta:
        managed = False
        db_table = 'player_language'

    def __str__(self):
        return "%s -> %s" % (self.player, self.language)


class Image(models.Model):
    player = models.ForeignKey(Player, primary_key=True)
    url    = models.TextField(primary_key=True)

    class Meta:
        managed  = False
        db_table = 'image'

    def __str__(self):
        return "%s: %s" % (self.player, self.url)


class Webresource(models.Model):
    player   = models.ForeignKey(Player, primary_key=True)
    facebook = models.TextField(blank=True)
    twitter  = models.TextField(blank=True)
    website  = models.TextField(blank=True)

    class Meta:
        managed  = False
        db_table = 'webresource'

    def __str__(self):
        return "%s: %s; %s; %s" % (self.player, self.facebook, 
                                   self.twitter, self.website)
