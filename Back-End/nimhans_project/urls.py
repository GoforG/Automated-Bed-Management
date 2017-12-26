"""Bed_Management URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.10/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import include, url
from django.contrib import admin
from rest_framework.urlpatterns import format_suffix_patterns
from bedapp import views

from rest_framework.routers import DefaultRouter

#router = DefaultRouter()
#router.register(r'devices', FCMDeviceViewSet)

urlpatterns = [
    url(r'^admin/', include(admin.site.urls)),
#    url(r'^', include(router.urls)),
    url(r'^login/', views.LoginModule.as_view()),
    url(r'^BedList/', views.ViewBedList.as_view()),
    url(r'^resident_form/', views.ResidentCheckReg.as_view()),
    url(r'^residentadmit_validate/',views.ResidentAdmitValidate.as_view()),
    url(r'^admit/', views.GetBedList.as_view()),
    url(r'^resident_new_date/', views.ResidentNewDate.as_view()),
    url(r'^bedSearch_ByConsultant/', views.BedSearch_ByConsultant.as_view()),
    url(r'^consultant_form/', views.ConsultantForm.as_view()),
    url(r'^nurse_transfer/', views.NurseTransferDone.as_view()),
    url(r'^logout/', views.LogoutModule.as_view()),
    url(r'^nurselogin/',views.NurseTable.as_view()),
    url(r'^resident_transfer_verify/',views.ResidentTransferVerify.as_view()),
    url(r'^resident_reply/',views.ResidentReply.as_view()),
    url(r'^request_display/',views.RequestDisplay.as_view()),
    url(r'^borrowed_bed_return',views.BorrowedBedReturn.as_view()),
    url(r'^make_responsible', views.MakeResponsible.as_view()),
    url(r'^change_ward', views.ChangeWard.as_view()),
    url(r'^forget_password', views.ForgetPassword.as_view()),
    url(r'^borrowrequest', views.BorrowRequest.as_view()),
    url(r'^new_password', views.SetPassword.as_view()),
    url(r'^checkforrollback', views.CheckForRollback.as_view()),
    url(r'^dorollback', views.DoRollback.as_view()),
    #url(r'^tmpdatafill',views.TempDataFill.as_view()),
    url(r'^freeretainbeds', views.FreeRetainedBeds.as_view()),
]
urlpatterns= format_suffix_patterns(urlpatterns)
