# -*- coding: utf-8 -*-
# Generated by Django 1.10.6 on 2017-10-02 21:00
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('bedapp', '0002_auto_20171002_2349'),
    ]

    operations = [
        migrations.AlterField(
            model_name='movement',
            name='inExpectedDaysInHospital',
            field=models.DateField(null=True),
        ),
        migrations.AlterField(
            model_name='movement',
            name='inExpectedDaysInWard',
            field=models.DateField(null=True),
        ),
    ]
