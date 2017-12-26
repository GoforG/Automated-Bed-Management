from django.contrib import admin
from .models import Employee, Patient, BedInfo, TransferTable, Movement , Checkup, PatientVisit,UserDeviceMapping, Request, Borrowed_Beds,Rollback,General_WardBedCount,General_UnitBedCount,Special_UnitCount

# Register your models here.
admin.site.register(Employee)
admin.site.register(Patient)
admin.site.register(BedInfo)
admin.site.register(TransferTable)
admin.site.register(Movement)
admin.site.register(Checkup)
admin.site.register(PatientVisit)
admin.site.register(UserDeviceMapping)
admin.site.register(Request)
admin.site.register(Borrowed_Beds)
admin.site.register(Rollback)
admin.site.register(General_WardBedCount)
admin.site.register(General_UnitBedCount)
admin.site.register(Special_UnitCount)

