from django import forms
from analytics.models import Nationality, Player, Style, Language

class SearchForm(forms.Form):
    name_pattern = forms.CharField(
        label        = 'Name'
        , max_length = 100
        , required   = False
    )
    birthdate_start = forms.DateField(
        label      = 'Birthdate'
        , required = False
    )
    birthdate_to = forms.DateField(
        label      = 'To'
        , required = False
    )
    club_pattern = forms.CharField(
        label        = 'Club'
        , max_length = 100
        , required   = False
    )
    nat_pattern = forms.CharField(
        label        = 'Nationality'
        , max_length = 100
        , required   = False
    )
    nationality = forms.ModelChoiceField(
        queryset   = Nationality.objects.all()
        , widget   = forms.Select(attrs={'class':'selector'})
        , required = False
    )
    hand = forms.ChoiceField(
        choices    = Player.HAND_CHOICES_AND_EMPTY
        , widget   = forms.Select(attrs={'class':'selector'})
        , required = False
    )
    style = forms.ModelChoiceField(
        queryset   = Style.objects.all()
        , widget   = forms.Select(attrs={'class':'selector'})
        , required = False
    )
    language = forms.ModelChoiceField(
        queryset   = Language.objects.all()
        , widget   = forms.Select(attrs={'class':'selector'})
        , required = False
    )

class GroupCountForm(forms.Form):
    group_count = forms.ChoiceField(
        choices = (
            ('hand', 'Hand')
            , ('club', 'Club')
            , ('coach', 'Coach')
            , ('nationality', 'Nationality')
            , ('gender', 'Gender')
        )
        , required = True
    )
