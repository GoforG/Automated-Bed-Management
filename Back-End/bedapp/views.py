from datetime import datetime,timedelta
from rest_framework.views import APIView
from .models import Employee, Patient, BedInfo, TransferTable, Movement , Checkup, UserDeviceMapping, Request, Borrowed_Beds, General_UnitBedCount, General_WardBedCount, Special_UnitCount, Rollback
from django.http import HttpResponse
from django.db import IntegrityError
from django.db import transaction
import json
import os,csv
import requests
from xml.etree import ElementTree
#import roman
from pyfcm import FCMNotification

waitList = []
push_service = FCMNotification(api_key="AAAAsovRMh4:APA91bFSbFTZF3mMZ768NC2ifO5_X1nTF5Vsvi3GNYN80ORtMXPldbJn74zbv8dw0W4F8Z7VeA_CES48L_ERY1Cd1u53BeezcWvWizM4pWquMZ_8eBC-4ai9xg2KAZAgf814sjXzFWMh")

# class TempDataFill:
#
#     @transaction.atomic
#     def post(self, request):
#         path = '/home/nimbm/Desktop/Bed_Data/'
#         for filename in os.listdir(path):
#             data = csv.reader(open(path+filename), delimiter=',', quotechar='"')
#
#         #    rowNum = 0
#
#             for row in data:
#                 for i in range(1,7):
#
#                     if row[i] is not None:
#                         url = "http://10.11.3.18/portalws/NIMHANSServiceContainerExtra?wsdl"
#                         headers = {'content-type': 'text/xml'}
#                         body = """<?xml version="1.0" encoding="UTF-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://ServiceContainer/">  <soapenv:Header/>
#                                         <soapenv:Body>
#                                             <ser:getPatDetailsByRegno>
#                                                 <in_token_str>dG9rZW5Ad2ViQGFwcG9pbnQjbmlj</in_token_str>
#                                                 <in_reg_no>""" + row[i] + """</in_reg_no>
#                                                 <in_hos_id/>
#                                             </ser:getPatDetailsByRegno>
#                                         </soapenv:Body>
#                                     </soapenv:Envelope>"""
#
#                         response = requests.post(url, data=body, headers=headers)
#                         root = ElementTree.fromstring(response.content)
#                         dat = root[0][0][0].text
#                         parsed_json = json.loads(dat)
#                         # print(parsed_json['data'])
#
#                         patient_data = parsed_json['data'][0]
#                         unit = patient_data['unit_name'].split()
#
#                         unit = roman.fromRoman(unit[1])
#
#                         patObj = Patient.objects.create(patientId=row[i], name=patient_data['p_name'],
#                                                sex=patient_data['p_sex'][0], unit = unit,
#                                                age=patient_data['p_age'], deptid=patient_data['dept_name'],
#                                                contactNo=patient_data['mobile_no'],
#                                                admitDate=patient_data['date_of_admission'])
#
#                         wardName = filename.split(".")[0]
#
#                         wardType = 'general'
#                         if wardName in ['fsr', 'msr', 'bd', 'a']:
#                             wardType = 'special'
#
#                         BedInfo.objects.create(bedId = wardName + str(row[0]),ward = wardName,
#                                                wardType = wardType,depid = str(1), currentUnit = str(i), status = 2, patientId = patObj)
#
#
#                         break
#
#                     elif i == 6:
#                         wardName = filename.split(".")[0]
#
#                         wardType = 'general'
#
#                         if wardName in ['fsr', 'msr', 'bd', 'a']:
#                             wardType = 'special'
#
#                         BedInfo.objects.create(bedId=wardName + str(row[0]), ward=wardName,
#                                                wardType=wardType, depid=str(1))
#         k = {'status': True}
#         k = json.dumps(k)
#         return HttpResponse(k)


class PendingNotifications:

    def __init__(self, userId,title,msgbody):
        self.userId = userId
        self.msgtitle = title
        self.msgbody = msgbody

class Messenger:

    @transaction.atomic
    def sendMessage(userId,message_title,message_body):
        global push_service
        global waitList
        UserDeviceobj = UserDeviceMapping.objects.filter(userId=userId)
        message_body = json.dumps(message_body)
        if not UserDeviceobj:
            waitList.append(PendingNotifications(userId, message_title, message_body))
        else:
            Userobj = UserDeviceobj.values('deviceId', 'userId')[0]
            if Userobj['deviceId'] == None:
                waitList.append(PendingNotifications(Userobj['userId'], message_title, message_body))
            else:
                push_service.notify_single_device(registration_id=Userobj['deviceId'],
                                                  message_title=message_title, message_body=message_body)



class LoginModule(APIView): ##this function is used to validate login of resident
    def get(self,request):
        pass

    @transaction.atomic
    def post(self, request):

        global push_service
        global waitList

        res = request.body.decode('utf-8')
        response = json.loads(res)

        userId = response['userId']
        pas = response['password']
        deviceId = response['deviceId']
        count = Employee.objects.filter(empId=userId, password=pas).count()

        if count==0:
            r = {'status': False, 'error_msg': 'Invalid Username or Password'}
            r = json.dumps(r)
            return HttpResponse(r)

        else:
            obj = Employee.objects.filter(empId=userId, password=pas).values('unit','empType','name','ward', 'responsible')[0]
            emptype = obj['empType']
            if obj['responsible']==0:
                resp='NO'
            else:
                resp='YES'
            if emptype == 'nurse':
                unit = obj['unit']
                ward = obj['ward']
                r1 = TransferTable.objects.filter(fromUnit = unit, fromWard=ward)  ### To display outgoing transfer requests from ward of nurse
                r2 = TransferTable.objects.filter(fromUnit = unit, toWard=ward)  ### To display incoming transfer requests to ward of nurse
                result = r1 | r2
                result = json.dumps(
                    list(result.values('patientId', 'name', 'fromUnit', 'fromWard', 'toWard', 'fromWardType', 'toWardType', 'fromBedId', 'toBedId',
                                       'timestamp')),
                    indent=4, sort_keys=True, default=str)
                print("result is " + result)
                k = {'status': True, 'table': result, 'empType':emptype,'empName':obj['name'], 'responsible':'NO'}

            else:

                k = {'status': True, 'empType': emptype,'empName':obj['name'], 'responsible':resp}

            k = json.dumps(k)

            UserDeviceMapping.objects.filter(deviceId=deviceId).update(deviceId = None)
            obj1 = UserDeviceMapping.objects.filter(userId=userId)

            if not obj1:
                UserDeviceMapping.objects.create(userId=userId, deviceId=deviceId)

            else:
                UserDeviceMapping.objects.filter(userId=userId).update(deviceId=deviceId)

            for w in waitList:

                if w.userId == userId:
                    push_service.notify_single_device(registration_id=deviceId, message_title=w.msgtitle,
                                                      message_body=w.msgbody)
                    waitList.remove(w)


            return HttpResponse(k)

class NurseTable(APIView): ## It gives list of transfers for particular unit to resident as well as nurses
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)
        nurseId = response['empId']

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=nurseId).values('deviceId')[0]

        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': False}
            k = json.dumps(k)
            return HttpResponse(k)

        empobj = Employee.objects.filter(empId=nurseId).values('ward', 'empType', 'unit')[0]
        ward = empobj['ward']
        empType = empobj['empType']

        if empType == 'resident': #means either resident needs transfer to view or nurse of sending ward needs transfer

            r1  = TransferTable.objects.filter(fromUnit = empobj['unit'], fromWard = ward)
            r2  = TransferTable.objects.filter(fromUnit = empobj['unit'], toWard=ward)
            r=r1|r2
            r = json.dumps(
                list(r.values('patientId', 'name', 'fromUnit', 'fromWard', 'toWard', 'fromWardType', 'toWardType', 'fromBedId', 'toBedId', 'timestamp')),
                indent=4, sort_keys=True, default=str)
            print(r)
            k = {'status': True, 'table': r}
            k = json.dumps(k)
            return HttpResponse(k)

        elif empType == 'nurse': #means nurse of receiving ward need transfer to view
            #####Please ensure that if one nurse confirms transaction, then that transaction shouldn't be visible to other nurses of same ward
            print("Nurses \n")
            r1 = TransferTable.objects.filter(fromUnit = empobj['unit'],fromWard=ward) ### To display outgoing transfer requests from ward of nurse
            r2 = TransferTable.objects.filter(fromUnit = empobj['unit'],toWard= ward) ### To display incoming transfer requests to ward of nurse
            result = r1|r2
            result = json.dumps(
                list(result.values('patientId', 'name', 'fromUnit', 'fromWard', 'toWard', 'fromBedId', 'toBedId', 'fromWardType', 'toWardType'
                              'timestamp')),
                indent=4, sort_keys=True, default=str)
            print("result is " +result)
            k = {'status': True, 'table': result}
            k = json.dumps(k)
            return HttpResponse(k)
        else:
            k = {'status': False}
            k = json.dumps(k)
            return HttpResponse(k)

class LogoutModule(APIView):  ##this function is used to validate logout of resident
    def get(self, request):
        pass

    @transaction.atomic
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)
        userId = response['empId']

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=userId).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': False}
            k = json.dumps(k)
            return HttpResponse(k)

        obj = UserDeviceMapping.objects.filter(userId = userId)
        if not obj:
            r = {'status': False}
            r = json.dumps(r)
            return HttpResponse(r)
        else:
            UserDeviceMapping.objects.filter(userId=userId).update(deviceId = None)
            r = {'status': True}
            r = json.dumps(r)
            return HttpResponse(r)


class ResidentCheckReg(APIView): ##this view appeaars at homescreen of admissions when doctor checks for patientid, used to check patient for admission
     def get(self,request):
        pass

     @transaction.atomic
     def post(self, request): #####expected admit date initially not in hospital data

        res = request.body.decode('utf-8')
        response = json.loads(res)
        empid = response['empId']
        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'logout'}
            k = json.dumps(k)
            return HttpResponse(k)



        patId = response['patientId']
        obj = Patient.objects.filter(patientId=patId).values('expAdmitDate','admitted','name','diagnosis','admitDate', 'unit')
        empobj = Employee.objects.filter(empId=empid).values()[0]
        if not obj:
            url = "http://10.11.3.18/portalws/NIMHANSServiceContainerExtra?wsdl"
            headers = {'content-type': 'text/xml'}
            body = """<?xml version="1.0" encoding="UTF-8"?><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://ServiceContainer/">  <soapenv:Header/>
                <soapenv:Body>
                    <ser:getPatDetailsByRegno>
                        <in_token_str>dG9rZW5Ad2ViQGFwcG9pbnQjbmlj</in_token_str>
                        <in_reg_no>"""+patId+"""</in_reg_no>
                        <in_hos_id/>
                    </ser:getPatDetailsByRegno>
                </soapenv:Body>
            </soapenv:Envelope>"""

            response = requests.post(url, data=body, headers=headers)
            root = ElementTree.fromstring(response.content)
            try:
                dat = root[0][0][0].text
                parsed_json = json.loads(dat)
                # print(parsed_json['data'])

                patient_data = parsed_json['data'][0]
                print(patient_data['unit_name'])
            except ValueError:
                r = {'status':'invalidId','msg':'Patient id is invalid'} #patient not registered
                r=json.dumps(r)
                return HttpResponse(r)

            Patient.objects.create(patientId = patId,name = patient_data['p_name'],sex = patient_data['p_sex'][0],unit = patient_data['unit_name'],
                                   age = patient_data['p_age'],deptid = patient_data['dept_name'],contactNo = patient_data['mobile_no'],admitDate = patient_data['date_of_admission'])
            obj = Patient.objects.filter(patientId=patId).values('expAdmitDate', 'admitted', 'name', 'diagnosis',
                                                                 'admitDate', 'unit')

        if obj[0]['unit'] != empobj['unit']:
            r = {'status': 'UnitDifferent',
                 'msg': 'Sorry the patient unit is different'}  # patient of different unit
            r = json.dumps(r)
        elif obj[0]['admitted'] == True:
            r={'status':'alreadyAdmitted','msg':'Sorry the patient is already admitted'}  #patient already admitted
            r=json.dumps(r)
        elif obj[0]['expAdmitDate'] is None:
            name=obj[0]['name']    #patient can be admitted or can be given date
            diag=obj[0]['diagnosis']
            r={'status':'admitting','patientId':patId,'name':name,'diagnosis':diag, 'unit':obj[0]['unit']}
            r=json.dumps(r)
        elif obj[0]['expAdmitDate'] > datetime.now().date():
            r={'status':'hasComeEarlier','msg':'patient has come earlier'}   #patient came earlier
            r=json.dumps(r)
        else:
            name=obj[0]['name']    #patient can be admitted or can be given date
            diag=obj[0]['diagnosis']
            r={'status':'admitting','patientId':patId,'name':name,'diagnosis':diag, 'unit':obj[0]['unit']}
            r=json.dumps(r)

        return HttpResponse(r)

class ViewBedList(APIView):  ## this view is called when resident wants to view availaible beds in other units and wards
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):

        res = request.body.decode('utf-8')
        response = json.loads(res)

        empId = response['empId']

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=empId).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        ward = response['location']
        unit = response['unit']
        wardType = response['wardType']

        if wardType == 'GENERAL':
            unitcountobj = General_UnitBedCount.objects.filter(unit = unit).values('threshold', 'filledCount')[0]
            wardcountobj = General_WardBedCount.objects.filter(ward = ward).values('totalCount', 'filledCount')[0]
            responsibelresidentobj = Employee.objects.filter(unit = unit, responsible = 1).values('empId', 'name')[0]
            #freebedobj = BedInfo.objects.filter(ward=ward, wardType=wardType, status = 0).values()
            #freebeds = len(freebedobj)
            freebeds = wardcountobj['totalCount'] - wardcountobj['filledCount']
            r = {'status':'True','wardtype':wardType, 'unitfilled':unitcountobj['filledCount'], 'unit-threshold':unitcountobj['threshold'],
                 'wardTotal':wardcountobj['totalCount'], 'wardfilled':wardcountobj['filledCount'], 'freebeds':freebeds,
                 'empid':responsibelresidentobj['empId'],'empname':responsibelresidentobj['name']}
            print (r)
            r = json.dumps(r)
            return HttpResponse(r)

        else:
            unitcountobj = Special_UnitCount.objects.filter(unit = unit, ward = ward).values('filledCount', 'totalCount')[0]
            responsibelresidentobj = Employee.objects.filter(unit=unit, responsible=1).values('empId', 'name')[0]
            specialBedObject = BedInfo.objects.filter(ward=ward, wardType=wardType).values()
            specialBedCount = len(specialBedObject)
            freespecialbedcount = 0
            for i in specialBedObject:
                if i['status'] == 0:
                    freespecialbedcount = freespecialbedcount + 1
            r = {'status':'True','wardtype':wardType, 'unitfilled':unitcountobj['filledCount'], 'unit-threshold':unitcountobj['totalCount'],
                 'total-beds':specialBedCount, 'freebeds':freespecialbedcount,
                 'empid':responsibelresidentobj['empId'],'empname':responsibelresidentobj['name']}
            r = json.dumps(r)
            return HttpResponse(r)

        r = {'status':'Error'}
        r = json.dumps(r)
        return HttpResponse(r)


class GetBedList(APIView):  ## this view is called when resident selects admit it is used to display list of empty beds as well as in case of return bed
    ### Changes were made on 29 nov 2017, to accomodate retained beds so that if any patient is gettin transferred to a ward, then only those
    ### beds are selected which were previously retained
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):

        res = request.body.decode('utf-8')
        response = json.loads(res)

        empId = response['empId']

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=empId).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        patobj = Patient.objects.filter(patientId = response['patientId']).values('sex')[0]
        if patobj['sex'] != response['location'][0]:
            if response['location'][0] == 'M' :
                k = {'status':'not_applicable','error_msg':'this ward admits males only'}
                k = json.dumps(k)
                print(k)
                return HttpResponse(k)
            elif response['location'][0] == 'F':
                k = {'status':'not_applicable','error_msg':'this ward admits females only'}
                k = json.dumps(k)
                print(k)
                return HttpResponse(k)

        ##choice = response['choice'] #choice = 0 matlab admit main bed status ke tym aur choice = 1 matlab incoming request ke tym


        loc = response['location'] ###destination location
        inUnit = response['unit']
        #wardtype = response['wardType']
        ##dep = response['depid'] maybe used for future purpose to ensure bed is allocated in same dept
        wardType = response['wardType'] #####ward type whether for special or general bed
        #r = {}

        if wardType == 'GENERAL' : ###when admission or transfer is done to general beds of the ward
            retainbedobj = BedInfo.objects.filter(status=3, ward=loc, wardType=wardType, patientId__patientId=response['patientId'] )
            #### Find all beds in the destination ward that are already retained by the patient so as to send only those beds in the list
            if len(retainbedobj) > 0:
                emptybedlist = json.dumps(list(retainbedobj.values('bedId')), indent=4, sort_keys=True, default=str)
                status = 'Y'
                r = {'wardType': wardType, 'status': status, 'emptybedlist': emptybedlist}
                r = json.dumps(r)
                return HttpResponse(r)
            else:
                obj = BedInfo.objects.filter(status = 0, ward = loc, wardType=wardType)
                emptybedlist = json.dumps(list(obj.values('bedId')), indent=4, sort_keys=True, default=str)
            #noofemptybeds = len(BedInfo.objects.filter(status = 0,ward = loc, currentUnit = inUnit, wardType = wardType))
            #noofbeds = len(BedInfo.objects.filter(ward = loc, currentUnit = inUnit, wardType = wardType))
            unitcountobj = General_UnitBedCount.objects.filter(unit = inUnit).values('threshold', 'filledCount')[0]
            unitcountavailaible = unitcountobj['threshold'] - unitcountobj['filledCount']
            print (unitcountavailaible)## this indicates wether unit has exceeded its availaible limit or not
            if len(obj)> 0 and unitcountavailaible > 0:
                status = 'Y'
                r = {'wardType': wardType, 'status': status, 'emptybedlist': emptybedlist}
            elif len(obj) <= 0:
                status = 'N'
                r = {'wardType': wardType, 'status': status, 'error_msg': 'No available beds in ward'}
                print(r)
                r = json.dumps(r)
                return HttpResponse(r)
            else:
                status='N'
                r = {'wardType': wardType, 'status': status, 'error_msg': 'You have reached your total bed count'}
                print(r)
                r = json.dumps(r)
                return HttpResponse(r)

        else :
            retainbedobj = BedInfo.objects.filter(status=3, ward=loc, wardType=wardType,
                                                  patientId__patientId=response['patientId'])
            #### Find all beds in the destination ward that are already retained by the patient so as to send only those beds in the list
            if len(retainbedobj) > 0:
                emptybedlist = json.dumps(list(retainbedobj.values('bedId')), indent=4, sort_keys=True, default=str)
                status = 'Y'
                r = {'wardType': wardType, 'status': status, 'emptybedlist': emptybedlist}
                r = json.dumps(r)
                return HttpResponse(r)
            UnitSpecialBeds = Special_UnitCount.objects.filter(unit = inUnit, ward = loc).values()[0]
            specialBedObject = BedInfo.objects.filter(ward = loc, wardType = wardType).values()
            specialBedCount = len(specialBedObject)
            ### updated on 29 nov 17, unitcount changed to 6 from 3
            unitCount = 3 ### no of units in the ward, also assumed each unit has fixed equal no of beds to begin with
            temp = int(specialBedCount/unitCount)
            movablesBed = specialBedCount - unitCount*temp  ### gives count of special beds which can be rotated in the given ward

            newObject = Special_UnitCount.objects.filter(ward = loc).values()

            var = 0 ## this variable will indicate whether there are any free movable beds in the ward or not
            for i in newObject:
                if (i['filledCount'] - i['totalCount']) > 0:
                    var += i['filledCount'] - i['totalCount']

            filled = UnitSpecialBeds['filledCount']
            total = UnitSpecialBeds['totalCount']

            if filled >= total and var >= movablesBed: #### filled >=  total means the unit has already exceeded its static count
                status = 'N'        #### var >= movablesbed means all of the available movable beds are already assigned so there is no bed to assign
                r = {'wardType': wardType, 'status': status, 'error_msg':'No free beds in ward'}

            else:       ## either this unit hasn't reached it's static bed count, like if bed = 10 and unit =3, then each unit has 3 beds , or there is atleast one movable bed free
                obj = BedInfo.objects.filter(ward=loc, status = 0, wardType=wardType)
                emptybedlist = json.dumps(list(obj.values('bedId')), indent=4, sort_keys=True, default=str)
                status = 'Y'

                r = {'wardType': wardType, 'status': status, 'emptybedlist': emptybedlist}

        r=json.dumps(r)
        return HttpResponse(r)

class ResidentAdmitValidate(APIView): ## this view is called when resident confirms bed selected to patient
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        patId = response['patientId']
        bedId = response['bedId']
        bedobj = BedInfo.objects.filter(bedId = bedId).values()[0]
        if response['currCond'] is None:
            curr = 50 #### By default,in the case when user dpesn't scroll the slider, so it is none
        else:
            curr = int(response['currCond'])    #converted to int
        # to be used later, diagnosis  = response['diagnosis']
        loc = response['location']
        wardtype = response['wardType']
        initialexpecteddays = int(response['daysexpected'])
        if initialexpecteddays is not None:
            initialexpecteddays = datetime.now().date() + timedelta(days=initialexpecteddays)
        #dep = response['depid']
        inUnit = response['unit'] #maybe used in future
        patNameobj = Patient.objects.filter(patientId = patId).values('name')[0]
        patName = patNameobj['name']
        r = {'status': 'Y'}
        #Patient.objects.filter(patientId = patId).update(diagnosis = diagnosis)
        try:
            ## referring foreign key patientId of transfer table through patNameobj
            patNameobj1 = Patient.objects.get(patientId = patId) ## getting patient object
            if bedobj['status'] != 3:
                BedInfo.objects.filter(bedId=bedId).update(status=0, currentUnit = None)

                if wardtype == 'SPECIAL':
                    specialfilledcount = Special_UnitCount.objects.filter(unit=inUnit, ward=loc).values()[0]['filledCount']
                    Special_UnitCount.objects.filter(unit=inUnit, ward=loc).update(filledCount=specialfilledcount + 1)
                else:
                    genwardfilledcount = General_WardBedCount.objects.filter(ward=loc).values()[0]['filledCount']
                    General_WardBedCount.objects.filter(ward=loc).update(filledCount=genwardfilledcount + 1)
                    genunitfilledcount = General_UnitBedCount.objects.filter(unit=inUnit).values()[0]['filledCount']
                    General_UnitBedCount.objects.filter(unit=inUnit).update(filledCount=genunitfilledcount + 1)
            TransferTable.objects.create(patientId=patNameobj1,name=patName,fromUnit=inUnit, fromWard=None,toWard=loc,fromBedId=None, toBedId=bedId,
                                         daysexpected=initialexpecteddays,condition=curr,
                                         fromWardType=None, toWardType=wardtype, lastverifiedward=loc)
            BedInfo.objects.filter(bedId = bedId).update(status = 1)
            #TransferTable.objects.filter(patientId__patientId=patId).update(lastverifiedward='general')####yahan maan rhe ki patient admit hone ke samay general mein jayega.
            ######### wened to send
            empobj = Employee.objects.filter(empType='nurse', ward=loc, unit=inUnit).values('empId')
            for w in empobj:
                message_title = "Admit Notification"
                message_body = {'for': 'nurse', 'msg': "Admit " + patId + " to ward " + loc}
                Messenger.sendMessage(w['empId'],message_title,message_body)
        except IntegrityError as e:
            r = {'status': 'N'} #saare integrity erros pakad raha hai but pakadna hume unique hai

        r=json.dumps(r)
        return HttpResponse(r)
        #now Transfer info to nurse

class ResidentNewDate(APIView):
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        patId = response['patientId']
        admitDate = response['admitDate']
        #curr = int(response['currCond'])
        Patient.objects.filter(patientId = patId).update(expAdmitDate = admitDate)
        r = {'status':']==M'}
        r = json.dumps(r)
        return HttpResponse(r)


class BedSearch_ByConsultant(APIView):  ####yahan ye check kar lo ki request karne wala ussi ward aur unit ka hai
    ### changed on 29 nov 2017
    def get(self,request): ##yeh dekh lena
        pass

    @transaction.atomic
    def post(self,request):

        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]

        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        empobj = Employee.objects.filter(empId = response['empId']).values('unit','ward', 'responsible')[0]

        bedToBeSearch = response['bed-id']
        obj = BedInfo.objects.filter(bedId = bedToBeSearch).values('patientId__patientId','ward','currentUnit', 'status')

        if len(obj) == 0:
            r = {'status': 'NOBEDEXISTS'}
            r = json.dumps(r)
            return HttpResponse(r)

        if obj[0]['currentUnit'] != empobj['unit']:
            r = {'status':'WRONGUNIT'}
            r = json.dumps(r)
            return HttpResponse(r)

        if obj[0]['ward'] != empobj['ward'] and empobj['responsible'] == 0:  ###So that responsible doctor can access all the beds within the unit
            r = {'status':'WRONGWARD'}
            r = json.dumps(r)
            return HttpResponse(r)

        patId = obj[0]['patientId__patientId']
        bedstatus = int(obj[0]['status'])

        if bedstatus == 0:
            r = {'status':'N.A.'}#not Allocated
            r = json.dumps(r)
            return HttpResponse(r)

        elif bedstatus == 1:
            r = {'status': 'Partial Booked'}  # not Allocated
            r = json.dumps(r)
            return HttpResponse(r)

        elif bedstatus == 3:
            r = {'status': 'Retained'}  # not Allocated
            r = json.dumps(r)
            return HttpResponse(r)

        else:
            ward = obj[0]['ward']
     #       print(loc)
            obj1 = Patient.objects.filter(patientId = patId).values('name', 'sex')[0]
            name = obj1['name']
            sex = obj1['sex']
            ####yhn se start hota dekhne ka, obj3 pehle use hua tha
            ##.... hum ye kar sakte hai ki latest checkup aur latest movement ka timestamp nikal le
            ##.... if checkuptime > movement time, then patient ki uss loc pe ek checkup ho chuka hai, so picchle checkup se values nikal lo
            ##.... else uska checkup na hua hai, then hum uske movement se data uthayenge
            ##.. also ydi uska checkup object hi na ho admit ke case me to direct movement se values utha lo
            obj2 = Checkup.objects.filter(patientId__patientId = patId)
            #obj3 = Checkup.objects.filter(patientId__patientId = patId).latest('timestamp')
            daysinhospital = 0
            daysInWard = 0
            if len(obj2)!=0:
                obj3= Checkup.objects.filter(patientId__patientId = patId).values('expDischargefromward','expDischargefromhosp').latest('timestamp')

                #    if loc != 'general':
                if obj3['expDischargefromward'] is None:
                        daysInWard = 0
                else:
                        daysInWard = ((obj3['expDischargefromward']) - datetime.now().date()).days
                        #     else:
                        #         daysInWard = None
                if obj3['expDischargefromhosp'] is None:
                        daysinhospital = 0
                else:
                        daysinhospital = ((obj3['expDischargefromhosp']) - datetime.now().date()).days

            #####yhn tak me dekhna, yeh picchli value nikalne ke liye tha

            r = {'status':'A','patientId':patId,'name':name,'loc':ward, 'sex':sex, 'daysinhospital':daysinhospital,'daysInWard':daysInWard}
            r = json.dumps(r)

            return HttpResponse(r)


class ConsultantForm(APIView): ###For storing requests in request table and sending notifications for transfer request and doing transfer in case of discharge
    ###changes on 29 nov 2017
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):

        res = request.body.decode('utf-8')
        response = json.loads(res)

        employee_id = response['empId']
        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=employee_id).values('deviceId')[0]

        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        patId = response['patientId']
        currCondition = int(response['currCond'])
        transferNeeded = response['transferNeeded']

        patientrequestobj = Request.objects.filter(patientId = patId)
        if len(patientrequestobj) != 0:
            k = {'status': 'ALREADY TRANSFER REQUEST'}
            k = json.dumps(k)
            return HttpResponse(k)
        obj = BedInfo.objects.filter(patientId__patientId=patId,status=2).values()[0]  # patient can have multiple beds so search on bed id which you will get from read qr directly

        samobj = Patient.objects.filter(patientId=patId).values('name', 'latestMovementId')[0]
        patName = samobj['name']
        movementtableid = samobj['latestMovementId']
        hospitaldischarge = response['hospitaldischarge']

        if hospitaldischarge is not None:
            hospitaldischarge = int(hospitaldischarge)
            hospitaldischarge = datetime.now().date() + timedelta(days=hospitaldischarge)

        Warddays = response['Warddays']
        if Warddays is not None:
            Warddays = int(Warddays)
            Warddays = datetime.now().date() + timedelta(days=Warddays)

        r = {'status': 'Success'}
        patNameobj = Patient.objects.get(patientId=patId)
        movementobj = Movement.objects.filter(Movementid=movementtableid).latest('Movementid')

        if transferNeeded:

            #BedInfo.objects.filter(patientId__patientId=patId).update(status=1) ##If transfer is cancelled, change status to 2 and also                change status of the bed which ispatially selected for transfer to 0
            transferTo = response['transferTo']
            daysInWardTransfer = response['daysInWardTransfer']
             ###done to check whether bed is retained or not
            #ward_type = response['ward_type']

            if daysInWardTransfer is not None:
                daysInWardTransfer = int(daysInWardTransfer)
                daysInWardTransfer = datetime.now().date() + timedelta(days=daysInWardTransfer)

            if transferTo == 'out': # days expected = 0 means date added to be is qual to 0
                #BedInfo.objects.filter(patientId__patientId=patId).update(status=1)
                TransferTable.objects.create(patientId=patNameobj, name=patName, daysexpected=datetime.now().date(),fromUnit=obj['currentUnit'], fromWard=obj['ward'], fromWardType = obj['wardType'] ,toWard='out',toWardType = None,fromBedId=obj['bedId'], toBedId=None, condition=currCondition, lastverifiedward = obj['ward'])

                Checkup.objects.create(patientmovementid=movementobj, patientId=patNameobj, condition=currCondition,expDischargefromhosp=datetime.now().date(), expDischargefromward=datetime.now().date())

                empobj = Employee.objects.filter(empType = 'nurse', ward = obj['ward'],unit = obj['currentUnit']).values('empId')

                for w in empobj:
                    message_title = "Discharge Notification"
                    message_body = {'for':'nurse',"msg":"Discharge "+patId+" from "+obj['ward']}
                    Messenger.sendMessage(w['empId'],message_title,message_body)

            else:
                ward_type = response['ward_type']
                from_msg = response['from_msg']
                retainstatus = response['retainstatus']
                if obj['ward'] == transferTo and retainstatus == 'YES':
                    r = {'status': 'error', 'error_msg': 'Bed cant be retained for within ward transfer'}
                    r = json.dumps(r)
                    return HttpResponse(r)
                #assumption ki har baar responsible resident millega
                responsible_resident_obj = Employee.objects.filter(empType = 'resident', responsible = 1, unit = obj['currentUnit']).values('empId')[0]

                if len(responsible_resident_obj) == 0: #if responsible resident not found
                    notifEmpObj = Employee.objects.filter(empType='resident', responsible=0,
                                            unit=obj['currentUnit']).values('empId')
                    for w in notifEmpObj:
                        message_title = "Make Responsible Notification"
                        message_body = {'for': 'residents', 'msg': 'Someone please become responsible for your ward' }
                        Messenger.sendMessage(w['empId'], message_title, message_body)

                    r = {'status':'no one responsible','error_msg':'Sorry no one is responsible for this ward'}
                    r = json.dumps(r)
                    return HttpResponse(r)

                responsible_resident = responsible_resident_obj['empId']
                residentnameobj = Employee.objects.filter(empId = employee_id).values('name')[0]
                ### return bed id by default is none and so is approval status pending
                #########isme towardtype daalna jo doctor se transfer screen pe lena padega
                Request.objects.create(patientId=patId, patient_name=patName, daysexpected=daysInWardTransfer, fromUnit=obj['currentUnit'],
                                       toUnit=obj['currentUnit'], fromWard=obj['ward'], toWard=transferTo, fromWardType = obj['wardType'],
                                       toWardType = ward_type, issuing_doctor_name = residentnameobj['name'], fromBedId=obj['bedId'],
                                       condition=currCondition, from_resident = employee_id, from_msg = from_msg, retainstatus=retainstatus)

           #     if obj['ward'] == 'general':
          #          Warddays = None
          #      else:
                Warddays = datetime.now().date()
                Checkup.objects.create(patientmovementid=movementobj, patientId=patNameobj, condition=currCondition,
                                       expDischargefromhosp=hospitaldischarge, expDischargefromward=Warddays)
                message_title = "Transfer Request Notification"
                message_body = {'for':'resident','msg':"To Transfer  " + patId + " from " + obj['ward'] + " to " + transferTo}
                Messenger.sendMessage(responsible_resident,message_title,message_body)

                r = {'status': 'Success'}
        else:
            #### Here also we need to check for ward days on the basis of location
            Checkup.objects.create(patientmovementid=movementobj, patientId=patNameobj, condition=currCondition,expDischargefromhosp=hospitaldischarge, expDischargefromward=Warddays)

        r = json.dumps(r)
        return HttpResponse(r)

class RequestDisplay(APIView): ####This displays pending,incoming and borrowing requests for resident
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)
        employee_id = response['empId']

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=employee_id).values('deviceId')[0]

        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': False}
            k = json.dumps(k)
            return HttpResponse(k)

        responsibleempobj = Employee.objects.filter(empId = employee_id).values('responsible', 'unit', 'ward','empId')[0]
        unit = responsibleempobj['unit']
        ward = responsibleempobj['ward']
        if responsibleempobj['responsible'] == 1:
            pending_request_obj = Request.objects.filter(fromUnit=unit)
            incoming_request_obj = Request.objects.filter(toUnit=unit).exclude(approval_status=2)
            borrrowing_request_obj = Borrowed_Beds.objects.filter(toUnit=unit)

            r1 = json.dumps(list(pending_request_obj.values()),indent=4, sort_keys=True, default=str)
            print(r1)
            r2 = json.dumps(list(incoming_request_obj.values()),
                                                             indent=4, sort_keys=True, default=str)
            print(r2)
            r3 = json.dumps(list(borrrowing_request_obj.values('borrowingid','timestamp','fromUnit','toUnit')),indent=4, sort_keys=True, default=str)
            #######fromUnit in Borrowed beds denotes unit from which we have taken bed and toUnit represents our unit
            #     print(r3)

            k = {'status': True, 'Responsible': 'yes' ,'ResEmp':responsibleempobj['empId'], 'pending_table': r1 , 'incoming_table':r2, 'borrowing_table':r3}

            k = json.dumps(k)
            #print (k)
            return HttpResponse(k)

        else:
            pending_request_obj = Request.objects.filter(fromUnit=unit, from_resident = employee_id, fromWard = ward)
            r1 = json.dumps(list(pending_request_obj.values()), indent=4, sort_keys=True, default=str)
            k = {'status': True, 'Responsible': 'no', 'ResEmp':responsibleempobj['empId'],'pending_table': r1,'incoming_table':None,'borrowing_table':None}
            print (r1)
            k = json.dumps(k)
            return HttpResponse(k)

class ResidentReply(APIView): ### if resident A request transfer from resident B, when B responds and either gives bed or rejects, this view is used
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT' }
            k = json.dumps(k)
            return HttpResponse(k)

        requestid = response['requestid']
        msg = response['returnmsg']
        requestobj = Request.objects.filter(requestid=requestid).values()[0]
        #bed_given = response['returnbed']
        #msg = response['returnmsg']
        status = response['replystatus'] #### 1 = approved, 2 =rejected 0 = pending 3 = borrowing
        #Request.objects.filter(requestid = requestid).update(to_msg = msg, approval_status = status, returnbedId = bed_given )
        senderobj =  Request.objects.filter(requestid = requestid).values('from_resident','patientId')[0]
        sender = senderobj['from_resident']
        #BedInfo.objects.filter(bedId=bed_given).update(status=1)
        message_title = "Transfer Response Notification"

        Request.objects.filter(requestid=requestid).update(to_msg=msg)
        if status == 1:
            if requestobj['requestType'] == 1:
                patid = requestobj['patientId']
                patunit = requestobj['fromUnit']
                transferobj = Request.objects.filter(patientId=patid, fromUnit=patunit, toUnit=patunit, approval_status=3)
                if len(transferobj)!=0:
                    Request.objects.filter(patientId=patid, fromUnit=patunit, toUnit=patunit, approval_status=3).update(approval_status=0)
                wardobj = General_WardBedCount.objects.filter(ward=requestobj['toWard']).values('totalCount', 'filledCount')[0]
                unitobj = General_UnitBedCount.objects.filter(unit=requestobj['toUnit']).values('threshold', 'filledCount')[0]
                if (wardobj['totalCount'] - wardobj['filledCount'] > 0) and (unitobj['threshold'] - unitobj['filledCount'] > 0):
                    Request.objects.filter(requestid=requestid).update(approval_status=1)


                    Borrowed_Beds.objects.create(fromUnit=requestobj['toUnit'], toUnit=requestobj['fromUnit'],
                                                 timestamp=datetime.now().date())
                    empobj = Employee.objects.filter(unit=requestobj['toUnit'], responsible=1).values('empId')[0]
                    responsibleemp = empobj['empId']
                    #Request.objects.filter(requestid=requestid).delete()
                    senderunit = requestobj['fromUnit']  ##3 sender means the resident which sent request first
                    receiverUnit = requestobj['toUnit']

                    senderobj = General_UnitBedCount.objects.filter(unit=senderunit).values('threshold')[0]
                    receiverobj = General_UnitBedCount.objects.filter(unit=receiverUnit).values('threshold')[0]
                    senderthreshold = senderobj['threshold'] + 1
                    receiverthreshold = receiverobj['threshold'] - 1

                    General_UnitBedCount.objects.filter(unit=senderunit).update(threshold=senderthreshold)
                    General_UnitBedCount.objects.filter(unit=receiverUnit).update(threshold=receiverthreshold)


                    message_title = "Borrowing Response Notification"
                    message_body = {'for': 'resident','msg': "Borrowing Approved for  " + requestobj['patientId'] + " from " + requestobj['toUnit']}
                    senderobj = Employee.objects.filter(unit=requestobj['fromUnit'], responsible=1).values('empId')[0] ##check if there are no responsible
                    sender = senderobj['empId']
                    Messenger.sendMessage(sender, message_title, message_body)

                elif (wardobj['totalCount'] - wardobj['filledCount']) <= 0:
                    k = {'status': 'FALSE', 'error_msg':'There are no available ward beds in pool'}
                    print (k)
                    k = json.dumps(k)
                    return HttpResponse(k)
                else:
                    k = {'status': 'FALSE', 'error_msg': 'You have already reached your bed count'}
                    print(k)
                    k = json.dumps(k)
                    return HttpResponse(k)

            else:
                bed_given = response['returnbed']
                Request.objects.filter(requestid=requestid).update(approval_status=1, returnbedId=bed_given)
                bedobj = BedInfo.objects.filter(bedId=bed_given).values('status')[0]
                if bedobj['status'] != 3:
                    BedInfo.objects.filter(bedId=bed_given).update(status=1, currentUnit=requestobj['fromUnit'])
                    requestobj = Request.objects.filter(requestid=requestid).values()[0]
                    towardtype = requestobj['toWardType']
                    toward = requestobj['toWard']
                    if towardtype == 'SPECIAL':
                        specialfilledcount = Special_UnitCount.objects.filter(unit = requestobj['toUnit'], ward=toward).values()[0]['filledCount']
                        Special_UnitCount.objects.filter(unit=requestobj['toUnit'], ward=toward).update(filledCount=specialfilledcount+1)
                    else:
                        genwardfilledcount = General_WardBedCount.objects.filter(ward=toward).values()[0]['filledCount']
                        General_WardBedCount.objects.filter(ward=toward).update(filledCount=genwardfilledcount + 1)
                        genunitfilledcount = General_UnitBedCount.objects.filter(unit=requestobj['toUnit']).values()[0]['filledCount']
                        General_UnitBedCount.objects.filter(unit=requestobj['toUnit']).update(filledCount=genunitfilledcount + 1)
                responsibleemp = Employee.objects.filter(unit=requestobj['fromUnit'], responsible=1).values('empId')[0]
                message_body = {'for':'resident','msg':"Transfer Approved for  " + senderobj['patientId'] + " from " + responsibleemp['empId']}
                Messenger.sendMessage(sender, message_title, message_body)


        else:
            Request.objects.filter(requestid=requestid).update(approval_status=status)
            if requestobj['requestType'] == 1:
                patid = requestobj['patientId']
                patobj = Patient.objects.get(patientId = patid)
                patunit = requestobj['fromUnit']
                #Request.objects.filter(patientId=patid, fromUnit=patunit, toUnit=patunit, approval_status=3).update(approval_status=0)
                transferobj = Request.objects.filter(patientId=patobj, fromUnit=patunit, toUnit=patunit, approval_status=3)
                if transferobj is not None:
                    Request.objects.filter(patientId=patid, fromUnit=patunit, toUnit=patunit, approval_status=3).update(approval_status=0)
                message_title = "Borrowing Response Notification "
                message_body = {'for': 'resident','msg': "Borrowing Rejected for  " + senderobj['patientId'] + " from Unit" + requestobj['toUnit']}
                Messenger.sendMessage(sender, message_title, message_body)
            else:
                responsibleemp = Employee.objects.filter(unit=requestobj['fromUnit'], responsible=1).values('empId')[0]
                message_body = {'for':'resident','msg':"Transfer Rejected for  " + senderobj['patientId'] + " from " + responsibleemp['empId']}
                Messenger.sendMessage(sender, message_title, message_body)



        k = {'status': 'TRUE'}
        k = json.dumps(k)
        return HttpResponse(k)


class BorrowRequest(APIView): ### This view is used when responsible resident makes borrowing request with another unit
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT' }
            k = json.dumps(k)
            return HttpResponse(k)

        borrowtype = response['borrowtype']
        msg = response['returnmsg']
        if borrowtype == 'transfer_borrowing':
            requestid = response['requestid']
            borrowingunit = response['unit'] ###unit to which borrowing request is done or made
            requestobj = Request.objects.filter(requestid=requestid).values()[0]
            if requestobj['approval_status'] == 3:
                k={'status':'FALSE', 'error_msg':'You have already made one borrowing request'}
                k=json.dumps(k)
                return  HttpResponse(k)
            fromunit=requestobj['fromUnit']
            responsibleemp=Employee.objects.filter(unit=fromunit, responsible=1).values('empId', 'name')[0]
            Request.objects.create(issuing_doctor_name=responsibleemp['name'], patientId=requestobj['patientId'], patient_name=requestobj['patient_name'],
                                   condition=requestobj['condition'], daysexpected=requestobj['daysexpected'], from_resident=responsibleemp['empId'],
                                   fromUnit=fromunit, toUnit=borrowingunit, fromWard=requestobj['fromWard'], toWard=requestobj['toWard'],
                                   fromWardType=requestobj['fromWardType'], toWardType=requestobj['toWardType'], fromBedId=requestobj['fromBedId'],
                                   approval_status=0, from_msg=msg, requestType=1)
            Request.objects.filter(requestid=requestid).update(approval_status=3)
            otherunitresidentobj = Employee.objects.filter(unit=borrowingunit, responsible=1).values('empId')[0]
            otherunitresident = otherunitresidentobj['empId']
            message_title = "Borrowing Request Notification"
            message_body = {'for': 'resident','msg': "Borrowing Request By Unit " + fromunit + " for patient " + requestobj['patient_name']}
            Messenger.sendMessage(otherunitresident, message_title, message_body)
            k = {'status': 'TRUE'}
            k = json.dumps(k)
            return HttpResponse(k)

        else:
            patientId=response['patientId']
            patientobj=Patient.objects.filter(patientId=patientId).values('name')[0]
            requestobj=Request.objects.filter(patientId=patientId)
            if len(requestobj) > 0:
                k = {'status': 'FALSE', 'error_msg':'You have already made one borrowing request'}
                k = json.dumps(k)
                return HttpResponse(k)
            fromunit=response['fromUnit']
            responsibleemp=Employee.objects.filter(unit=fromunit, responsible=1).values('empId', 'name', 'ward')[0]
            print(response['toWard']+' '+response['toWardType']+' '+msg)
            Request.objects.create(issuing_doctor_name=responsibleemp['name'], patientId=patientId,patient_name=patientobj['name'],
                                   condition=response['condition'], daysexpected=datetime.now().date()+timedelta(days = int(response['daysexpected'])),
                                   from_resident=responsibleemp['empId'],
                                   fromUnit=fromunit, toUnit=response['toUnit'], fromWard=None,toWard=response['toWard'],
                                   fromWardType=None, toWardType=response['toWardType'],fromBedId=None,
                                   approval_status=0, from_msg=msg, requestType=1)
            k = {'status': 'TRUE'}
            k = json.dumps(k)
            return HttpResponse(k)



class ResidentTransferVerify(APIView):### if resident A request transfer from resident B, when A responds and either gives transfer order to nurses or rejects, this view is used, Also this is used to verify beds which are borrowed
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': False}
            k = json.dumps(k)
            return HttpResponse(k)

        requestid = response['requestid']
        sender_status = response['replystatus']##can be 1 if resident A rejects , 2 if A accepts and 0 if already B don't gives bed to A and can be 3 for borrowing unit
        requestobj = Request.objects.filter(requestid = requestid).values()[0] ####In doubt, we need all values
        if sender_status == "rejected": #sender resident rejected the request ####front end pe change karo
            bedobj = BedInfo.objects.filter(bedId=requestobj['returnbedId']).values('staus')[0]
            if bedobj['status'] != 3:
                BedInfo.objects.filter(bedId=requestobj['returnbedId']).update(status=0, currentUnit = None)
                requestobj = Request.objects.filter(requestid=requestid).values()[0]
                towardtype = requestobj['toWardType']
                toward = requestobj['toWard']
                if towardtype == 'SPECIAL':
                    specialfilledcount = Special_UnitCount.objects.filter(unit=requestobj['toUnit'], ward=toward).values()[0]
                    Special_UnitCount.objects.filter(unit=requestobj['toUnit'], ward=toward).update(filledCount=specialfilledcount - 1)
                else:
                    genwardfilledcount = General_WardBedCount.objects.filter(ward=toward).values()[0]['filledCount']
                    General_WardBedCount.objects.filter(ward=toward).update(filledCount=genwardfilledcount + 1)
                    genunitfilledcount = General_UnitBedCount.objects.filter(unit=requestobj['toUnit']).values()[0]['filledCount']
                    General_UnitBedCount.objects.filter(unit=requestobj['toUnit']).update(filledCount=genunitfilledcount - 1)
            empobj = Employee.objects.filter(unit=response['fromUnit'], responsible=1).values('empId')[0]
            responsibleemp = empobj['empId']
            Request.objects.filter(requestid = requestid).delete()
            message_title = "Bed not needed"
            message_body = {'for':'resident','msg':requestobj['returnbedId']+ " Bed not needed for " + requestobj['patientId']}
            Messenger.sendMessage(responsibleemp,message_title,message_body)
        elif sender_status == "accepted": #sender resident accepted the request
            Request.objects.filter(requestid=requestid).delete()
            patientid = requestobj['patientId']
            patientforeignobj = Patient.objects.get(patientId=patientid)
            TransferTable.objects.create(patientId = patientforeignobj, name = requestobj['patient_name'], fromUnit = requestobj['fromUnit'],
                                         fromWard = requestobj['fromWard'],toWard = requestobj['toWard'], fromBedId = requestobj['fromBedId'],
                                         toBedId = requestobj['returnbedId'],condition = requestobj['condition'],fromWardType=requestobj['fromWardType'],
                                         toWardType=requestobj['toWardType'],daysexpected = requestobj['daysexpected'],
                                         lastverifiedward = requestobj['fromWard'], retainstatus = requestobj['retainstatus'])
            ##if requestobj['requestType'] == 1: ####if borrowing request, then we need to change current unit of borrowed bed to our unit, also technically this should be done when nurse actually transfers the patient , but right now we will look into future
            BedInfo.objects.filter(bedId=requestobj['returnbedId']).update(currentUnit=requestobj['fromUnit']) #### changing current unit of borrowed bed, current unit basicallly tells which unit bed currently belongs to and for looking availaible beds in a unit, we will just search on current unit
            empobj = Employee.objects.filter(unit=response['fromUnit'], responsible=1).values('empId')[0]
            responsibleemp = empobj['empId']
            message_title = "Bed accepted"
            message_body = {'for': 'resident','msg': requestobj['returnbedId'] + " Bed accepted for " + requestobj['patientId']}
            Messenger.sendMessage(responsibleemp, message_title, message_body)
            ##BorrowedBeds.objects.create(bedId = requestobj['returnbedId'],borrower_residentId = empobj['empId'] ,fromUnit = requestobj['fromUnit'],toUnit = requestobj['toUnit'] ,fromWard = requestobj['toWard'] ,toWard = requestobj['toWard'] )####fromUnit = requestobj['fromUnit'] ye galti nhi hai
            senderresident = Employee.objects.filter(empId = requestobj['from_resident']).values('unit', 'ward')[0]
            ##receiverresident = Employee.objects.filter(empId=requestobj['to_resident']).values('unit', 'ward')[0]
            nurseinsenderward = Employee.objects.filter(empType='nurse', ward= senderresident['ward'], unit= senderresident['unit']).values('empId')
            nurseinreceiverward = Employee.objects.filter(empType='nurse', ward= requestobj['toWard'], unit= requestobj['fromUnit']).values('empId')
            for w in nurseinsenderward:
                message_title = "Transfer Notification"
                message_body = {'for':'nurse','msg':"Transfer " + requestobj['patientId'] + " from " + requestobj['fromBedId'] + " to " + requestobj['returnbedId']}
                Messenger.sendMessage(w['empId'],message_title,message_body)

            if senderresident['ward'] != requestobj['toWard']:  #when same ward transfer then no repeat request
                for w in nurseinreceiverward:
                    message_title = "Transfer Notification"
                    message_body = {'for':'nurse','msg':"Transfer " + requestobj['patientId'] + " from " + requestobj['fromBedId'] + " to " + requestobj['returnbedId']}
                    Messenger.sendMessage(w['empId'], message_title, message_body)

        elif sender_status == "delete_request_or_borrow_bed": ###used to delete request in case of intra unit and for borrowing in case of inter unit
                Request.objects.filter(requestid=requestid).delete()
        ##elif sender_status == 3: # for borrowing request
        ##    empobj = Employee.objects.filter(unit = response['toUnit'],ward = requestobj['toWard'],responsible = 1).values('empId','name')[0]
        ##    ####When a attempts to borrow, we change the currnet request to borrowing type by changing approval status to pending, and making request type to 1(inter transfer)
        ##    Request.objects.filter(requestid=requestid).update(requestType=1,approval_status=0,to_resident = empobj['empId'],toUnit = response['toUnit'],to_msg = None) ####pending request
        ##    message_title = "Borrowing Request Notification"
        ##    message_body = {'for':'resident','msg':"To Borrow bed to Transfer  " + requestobj['patientId'] + " from " + requestobj['fromUnit']+" and ward = "+requestobj['fromWard']}
        ##    Messenger.sendMessage(empobj['empId'], message_title, message_body)

        k = {'status': True}
        k = json.dumps(k)
        return HttpResponse(k)

class NurseTransferDone(APIView):
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        nurseId = response['empId']
        patientId = response['patientId']
        nurseobj = Employee.objects.filter(empId = nurseId).values()[0]
        transferobj = TransferTable.objects.filter(patientId__patientId = patientId).values()[0]
        nurseward = nurseobj['ward']
        if nurseward == transferobj['lastverifiedward']:
            if nurseward == transferobj['toWard']:
                #do something, send notification to sending and receiving residents by adding them to transfer table
                TransferTable.objects.filter(patientId__patientId=patientId).update(lastverifiedward=None)
                FinalSubmit.submit(status = 'transfer', patientId = patientId )
            else:
                #do other thinge
                TransferTable.objects.filter(patientId__patientId=patientId).update(lastverifiedward = transferobj['toWard'])
                if transferobj['toWard'] == 'out':
                    FinalSubmit.submit(status = 'discharge',patientId = patientId)
        else:
            r = {'status': 'Failure', 'error_msg':'Currently you are not allowed for transfer validation'}
            r = json.dumps(r)
            return HttpResponse(r)
        r = {'status': 'Success'}
        r = json.dumps(r)
        return HttpResponse(r)


class FinalSubmit(): ## It is used to confirm transfers and reflect them in movement table #######baad me dekhte hai, filled count ghatana badhaana hai
    ### updated on 29 nov 2017
    ### here it is assumed on rollback, only present bed is rollbacked and all other beds retained by patient are not allocated and are freed
    @transaction.atomic
    def submit(status, patientId):

        patTransfer = TransferTable.objects.filter(patientId__patientId = patientId).values()[0] ## referiing foreign key through __ method


        if status == 'discharge':
            bedId = patTransfer['fromBedId']
            generalbedcountobj = BedInfo.objects.filter(patientId__patientId=patientId, wardType = 'GENERAL').values('ward')
            generalbedcount = len(generalbedcountobj)
            specialbedcountobj = BedInfo.objects.filter(patientId__patientId=patientId, wardType='SPECIAL').values('ward')
            specialbedcount = len(specialbedcountobj)
            empUnit = patTransfer['fromUnit']
            BedInfo.objects.filter(patientId__patientId=patientId).update(patientId=None, status=0,
                                                                          currentUnit=None)  # bed status to be changed to 0
            Movement.objects.filter(patientId=patientId, outDate=None).update(outDate=datetime.now().date(),
                                                                              outCondition=patTransfer[
                                                                                  'condition'])  ##referring foreign key through patient object
            Patient.objects.filter(patientId=patientId).update(admitted=False)
            for i in generalbedcountobj:
                generalwardobj = General_WardBedCount.objects.filter(ward=i['ward']).values('filledCount', 'totalCount')[0]
                filled = generalwardobj['filledCount'] - 1
                General_WardBedCount.objects.filter(ward=i['ward']).update(filledCount=filled)
            for i in specialbedcountobj:
                specialwardobj = Special_UnitCount.objects.filter(unit=empUnit, ward=i['ward']).values('filledCount', 'totalCount')[0]
                filled = specialwardobj['filledCount'] - specialbedcount
                Special_UnitCount.objects.filter(unit=empUnit, ward=i['ward']).update(filledCount=filled)

            generalunitobj = General_UnitBedCount.objects.filter(unit=empUnit).values('filledCount')[0]
            filled = generalunitobj['filledCount'] - generalbedcount
            General_UnitBedCount.objects.filter(unit=empUnit).update(filledCount=filled)


            responsibleresident = Employee.objects.filter(unit=empUnit, responsible=1).values('empId')[0]
            message_title = "Transfer Notification"
            message_body = {'for': 'Responsible Resident',
                            'msg': "Discharge for " + patientId + " from " + patTransfer['fromBedId'] + " Successful "}
            Messenger.sendMessage(responsibleresident['empId'], message_title, message_body)

            ## checkup can be updated for discharge
        elif status == 'transfer':
            obj = Movement.objects.filter(patientId = patientId).values()
            if not obj:
                toBedId = patTransfer['toBedId']
                patNameobj = Patient.objects.get(patientId = patientId)
                BedInfo.objects.filter(bedId = toBedId).update(patientId = patNameobj,status = 2, currentUnit=patTransfer['fromUnit']) #change status of bed
                Movement.objects.create(patientId = patientId,ward = patTransfer['toWard'],wardType = patTransfer['toWardType'],inDate = datetime.now().date(),
                                        outDate = None,inCondition = patTransfer['condition'],outCondition = None, inExpectedDaysInWard = None,
                                        inExpectedDaysInHospital = patTransfer['daysexpected'])
                #obj1 = Movement.objects.filter(patientId = patientId).values().latest('Movementid')##############[-1]
                #obj1 = Movement.objects.filter(patientId = patientId).latest('Movementid')
                #patNameobj = Patient.objects.get(patientId = patientId)
                #Checkup.objects.create(patientmovementid = obj1, patientId=patNameobj, condition=patTransfer['condition'], expDischargefromhosp = patTransfer['daysexpected'], expDischargefromward = None)
                Patient.objects.filter(patientId = patientId).update(admitted= True, admitDate = datetime.now().date())
                empUnit = patTransfer['fromUnit']
                ##########################################################
                empUnit = patTransfer['fromUnit']
                empward = patTransfer['toWard']
                empwardtype = patTransfer['toWardType']
                # if empwardtype == 'SPECIAL':
                #     specialwardobj = Special_UnitCount.objects.filter(unit=empUnit, ward=empward).values('filledCount', 'totalCount')[0]
                #     filled = specialwardobj['filledCount'] + 1
                #     Special_UnitCount.objects.filter(unit=empUnit, ward=empward).update(filledCount=filled)
                # else:
                #     generalwardobj = General_WardBedCount.objects.filter(ward=empward).values('filledCount', 'totalCount')[0]
                #     filled = generalwardobj['filledCount'] + 1
                #     General_WardBedCount.objects.filter(ward=empward).update(filledCount=filled)
                #     generalunitobj = General_UnitBedCount.objects.filter(unit=empUnit).values('filledCount')[0]
                #     filled = generalunitobj['filledCount'] + 1
                #     General_UnitBedCount.objects.filter(unit=empUnit).update(filledCount=filled)
                ###############################################################

                responsibleresident = Employee.objects.filter(unit=empUnit, responsible=1).values('empId')[0]
                message_title = "Transfer Notification"
                message_body = {'for': 'Responsible Resident',
                                'msg': "Admission for " + patientId + " to " + patTransfer['toBedId'] + " Successful "}
                Messenger.sendMessage(responsibleresident['empId'], message_title, message_body)

            else:
                fromBedId = patTransfer['fromBedId']
                toBedId = patTransfer['toBedId']
                retainstatus = patTransfer['retainstatus']
                if retainstatus == 'YES':
                    BedInfo.objects.filter(bedId = fromBedId).update(status=3)
                else:
                    BedInfo.objects.filter(bedId = fromBedId).update(patientId = None, status = 0, currentUnit=None)# general bed allotment, patient can have general as well as icu bed
                patNameobj = Patient.objects.get(patientId = patientId)
                BedInfo.objects.filter(bedId = toBedId).update(patientId = patNameobj, status = 2, currentUnit=patTransfer['fromUnit'])# change status of bed appropriatel ,ie.e general -special,special -> special etc
                #BedInfo.objects.filter(bedId = fromBedId).update(status = 0)
                #BedInfo.objects.filter(bedId = toBedId).update(status = 2)
                Movement.objects.filter(patientId = patientId,outDate = None).update(outDate = datetime.now().date(),outCondition = patTransfer['condition'])
                Movement.objects.create(patientId = patientId,ward = patTransfer['toWard'],wardType = patTransfer['toWardType'], outDate = None,
                                        inCondition = patTransfer['condition'],outCondition = None, inExpectedDaysInWard = patTransfer['daysexpected'])
                #obj1 = Movement.objects.filter(patientId = patientId).values().latest('Movementid')###################[-1]
                #obj1 = Movement.objects.filter(patientId = patientId).latest('Movementid')
                #if patTransfer['toWard'] == 'general':  #considering general discharge = hosp discharge
                #    Checkup.objects.create(patientmovementid = obj1, patientId=patNameobj, condition=patTransfer['condition'], expDischargefromhosp = patTransfer['daysexpected'], expDischargefromward = None)
                #else:
                #    obj2 = Checkup.objects.filter(patientId__patientId = patientId).values().latest('id')##############[-1]
                #    Checkup.objects.create(patientmovementid = obj1, patientId=patNameobj, condition=patTransfer['condition'], expDischargefromhosp = obj2['expDischargefromhosp'], expDischargefromward = patTransfer['daysexpected'])
                empUnit = patTransfer['fromUnit']
                responsibleresident = Employee.objects.filter(unit=empUnit, responsible=1).values('empId')[0]

                ########################################
                empfromUnit = patTransfer['fromUnit']
                empfromward = patTransfer['fromWard']
                empfromwardtype = patTransfer['fromWardType']

                if retainstatus == 'NO':
                    if empfromwardtype == 'SPECIAL':
                        specialwardobj = Special_UnitCount.objects.filter(unit=empfromUnit, ward=empfromward).values('filledCount', 'totalCount')[0]
                        filled = specialwardobj['filledCount'] - 1
                        Special_UnitCount.objects.filter(unit=empfromUnit, ward=empfromward).update(filledCount=filled)
                    else:
                        generalwardobj = General_WardBedCount.objects.filter(ward=empfromward).values('filledCount', 'totalCount')[0]
                        filled = generalwardobj['filledCount'] - 1
                        General_WardBedCount.objects.filter(ward=empfromward).update(filledCount=filled)
                        generalunitobj = General_UnitBedCount.objects.filter(unit=empfromUnit).values('filledCount')[0]
                        filled = generalunitobj['filledCount'] - 1
                        General_UnitBedCount.objects.filter(unit=empfromUnit).update(filledCount=filled)

                # emptoUnit = patTransfer['toUnit']
                # emptoward = patTransfer['toWard']
                # emptowardtype = patTransfer['toWardType']
                # if emptowardtype == 'SPECIAL':
                #     specialwardobj = Special_UnitCount.objects.filter(unit=empfromUnit, ward=emptoward).values('filledCount', 'totalCount')[0]
                #     filled = specialwardobj['filledCount'] + 1
                #     Special_UnitCount.objects.filter(unit=empfromUnit, ward=emptoward).update(filledCount=filled)
                # else:
                #     generalwardobj = General_WardBedCount.objects.filter(ward=emptoward).values('filledCount', 'totalCount')[0]
                #     filled = generalwardobj['filledCount'] + 1
                #     General_WardBedCount.objects.filter(ward=emptoward).update(filledCount=filled)
                #     generalunitobj = General_UnitBedCount.objects.filter(unit=empfromUnit).values('filledCount')[0]
                #     filled = generalunitobj['filledCount'] + 1
                #     General_UnitBedCount.objects.filter(unit=empfromUnit).update(filledCount=filled)
                #################################################

                message_title = "Transfer Notification"
                message_body = {'for': 'Responsible Resident',
                                'msg': "Transfer for " + patientId + " from " + patTransfer[
                                    'fromBedId'] + " to " +
                                       patTransfer['toBedId'] + " Successful "}
                Messenger.sendMessage(responsibleresident['empId'], message_title, message_body)
        ####
        obj1 = Movement.objects.filter(patientId = patientId).values().latest('Movementid')############[-1]
        Patient.objects.filter(patientId = patientId).update(latestMovementId = obj1['Movementid'],loc = patTransfer['toWard'], wardType=patTransfer['toWardType'])
        rollbackobj = Rollback.objects.filter(patientId=patientId).values()
        if len(rollbackobj)!=0:
            Rollback.objects.filter(patientId=patientId).delete()
        Rollback.objects.create(patientId=patientId, fromWard=patTransfer['fromWard'], toWard = patTransfer['toWard'], fromWardType=patTransfer['fromWardType'],
                                toWardType=patTransfer['toWardType'], fromBedId=patTransfer['fromBedId'], toBedId=patTransfer['toBedId'], fromUnit=patTransfer['fromUnit'])
        TransferTable.objects.filter(patientId__patientId=patientId).delete()

class AdmitDate(APIView):
    def get(self,request):
        pass

    @transaction.atomic
    def post(self,request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        ward = response['ward']
        unit = response['unit']
        bedInfo = BedInfo.objects.filter(location=ward, unit=unit)
        bedInfo = json.dumps(list(bedInfo.values('bedId','location','unit','patientId')), indent=4, sort_keys=True, default=str)

        return HttpResponse(bedInfo)

class BorrowedBedReturn(APIView): ### send borrowing id from app

    @transaction.atomic
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        ##print("\n \n"+response['bedId'])
        ##bedobj = BedInfo.objects.filter(bedId = response['bedId']).values('unit','status')[0]
        borrowobject = Borrowed_Beds.objects.filter(borrowingid = response['borrowId']).values()[0]
        borrowingunit = borrowobject['fromUnit']
        borrowerunit = borrowobject['toUnit']

        borrowingunitbedcountobj = General_UnitBedCount.objects.filter(unit = borrowingunit).values()[0]
        #borrowingunitfilled = borrowingunitbedcountobj['filledCount']
        borrowingunitthreshold = borrowingunitbedcountobj['threshold']

        borrowerunitbedcountobj = General_UnitBedCount.objects.filter(unit=borrowerunit).values()[0]
        borrowerunitthreshold = borrowerunitbedcountobj['threshold']
        borrowerunitfilled = borrowerunitbedcountobj['filledCount']

        if borrowerunitfilled >= borrowerunitthreshold: ###check whether bed is empty or not, i.ee there shouldn't be any patient currently on the bed , for 0 bed is not vacant
            r = {'status': 'failed','error_msg':'A bed is yet to be released, Please release any bed and then continue'}
            r = json.dumps(r)
            return HttpResponse(r)
        else:
            Borrowed_Beds.objects.filter(borrowingid = response['borrowId']).delete()
            #BedInfo.objects.filter(bedId = response['bedId']).update(currentUnit = bedobj['unit'])
            borrowingunitthreshold = borrowingunitthreshold + 1
            borrowerunitthreshold = borrowerunitthreshold - 1
            General_UnitBedCount.objects.filter(unit=borrowingunit).update(threshold=borrowingunitthreshold)
            General_UnitBedCount.objects.filter(unit=borrowerunit).update(threshold=borrowerunitthreshold)
            r = {'status': 'Success'}
            r = json.dumps(r)
            return HttpResponse(r)

class CheckForRollback(APIView):

    @transaction.atomic
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        employeeId = response['empId']

        currDeviceId = UserDeviceMapping.objects.filter(userId=employeeId).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        patId = response['patientId']
        patobj = Patient.objects.filter(patientId=patId).values()

        if len(patobj)==0:
            k = {'status': 'invalid','error_msg':'invalid patient id'}
            k = json.dumps(k)
            return HttpResponse(k)

        patobj = patobj[0]
        # rollbackobj = Rollback.objects.filter(patientId=patId).values()
        requestobj = Request.objects.filter(patientId=patId).values()
        transferobj = TransferTable.objects.filter(patientId=patobj).values()
        checkuptimelist = Checkup.objects.filter(patientId=patId).values()
        rollbackobjlist = Rollback.objects.filter(patientId=patId).values()
        if len(checkuptimelist)!=0:
            checkuptime = Checkup.objects.filter(patientId=patId).values().latest('timestamp')
        if len(rollbackobjlist)!=0:
            rollbackobj = Rollback.objects.filter(patientId=patId).values().latest('timestamp')

        if len(rollbackobjlist)!=0:
            bedstatusobj = BedInfo.objects.filter(bedId=rollbackobj['fromBedId']).values()
        if len(rollbackobjlist)==0:
            k = {'status': 'norollback', 'error_msg': 'Sorry no record for rollback exists'}
        elif len(requestobj) != 0:
            k = {'status': 'request_exist', 'error_msg': 'Sorry request for the given patient exist'}
        elif len(transferobj) != 0:
            k = {'status': 'transfer_exist', 'error_msg': 'Sorry transfer request for the given patient exist'}
        elif len(checkuptimelist)!=0 and len(rollbackobjlist)!=0  and checkuptime['timestamp'] > rollbackobj['timestamp']:
            k = {'status': 'checkup_exist', 'error_msg': 'Sorry already a checkup has been done on this bed'}
        elif len(bedstatusobj) != 0 and (bedstatusobj[0]['status'] == 1 or bedstatusobj[0]['status'] == 2 or
                                             (bedstatusobj[0]['status']==3 and bedstatusobj[0]['patientId_id'] != patId)):
            #print(bedstatusobj[0])
            k = {'status': 'bedfilled', 'error_msg': 'Sorry rollback bed not vacant '}
        else:
            #rollbackdata = json.dumps(rollbackobj)
            k = {'status':'rollbackdata','name':patobj['name'],'patientId':patId,'fromUnit':rollbackobj['fromUnit'],'fromWard':rollbackobj['fromWard'],
                 'toWard':rollbackobj['toWard'],'fromBedId':rollbackobj['fromBedId'],'toBedId':rollbackobj['toBedId'],'fromWardType':rollbackobj['fromWardType'],
                 'toWardType':rollbackobj['toWardType']}
        k = json.dumps(k)
        return HttpResponse(k)

class DoRollback(APIView):

    @transaction.atomic
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        employeeId = response['empId']

        currDeviceId = UserDeviceMapping.objects.filter(userId=employeeId).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        patId = response['patientId']
        patobj = Patient.objects.filter(patientId=patId)[0]
        #rollbackobj = Rollback.objects.filter(patientId=patId).values()
        requestobj = Request.objects.filter(patientId=patId).values()
        transferobj = TransferTable.objects.filter(patientId=patobj).values()

        checkuptimelist = Checkup.objects.filter(patientId=patId).values()
        rollbackobjlist = Rollback.objects.filter(patientId=patId).values()
        if len(checkuptimelist)!=0:
            checkuptime = Checkup.objects.filter(patientId=patId).values().latest('timestamp')
        if len(rollbackobjlist)!=0:
            rollbackobj = Rollback.objects.filter(patientId=patId).values().latest('timestamp')

        #rollbackbedobj = Rollback.objects.filter(patientId=patId).values('fromBedId')[0]
        if len(rollbackobjlist)!=0:
            bedstatusobj = BedInfo.objects.filter(bedId=rollbackobj['fromBedId']).values()
        if len(rollbackobjlist) == 0:
            k = {'status': 'norollback', 'error_msg': 'Sorry no record for rollback exists'}
        elif len(requestobj) != 0:
            k = {'status': 'request_exist', 'error_msg': 'Sorry request for the given patient exist'}
        elif len(transferobj) != 0:
            k = {'status': 'transfer_exist', 'error_msg': 'Sorry transfer request for the given patient exist'}
        elif len(checkuptimelist)!=0 and len(rollbackobjlist)!=0 and checkuptime['timestamp'] > rollbackobj['timestamp']:
            k = {'status': 'checkup_exist', 'error_msg': 'Sorry already a checkup has been done on this bed'}
        elif len(bedstatusobj)!=0 and (bedstatusobj[0]['status'] == 1 or bedstatusobj[0]['status'] == 2 or
                                             (bedstatusobj[0]['status']==3 and bedstatusobj[0]['patientId_id'] != patId)):
            k = {'status': 'bedfilled', 'error_msg': 'Sorry rollback bed not vacant '}
        else:
            if rollbackobj['fromBedId'] is None: #admit rollback
                if(rollbackobj['toWardType']=='SPECIAL'):
                    spfilledcount = Special_UnitCount.objects.filter(unit = rollbackobj['fromUnit'],
                                                                     ward = rollbackobj['toWard']).values()[0]['filledCount']
                    Special_UnitCount.objects.filter(unit = rollbackobj['fromUnit'],
                                                     ward = rollbackobj['toWard']).update(filledCount = spfilledcount-1)
                else:
                    genwardfilledcount = General_WardBedCount.objects.filter(ward=rollbackobj['toWard']).values()[0]['filledCount']
                    General_WardBedCount.objects.filter(ward=rollbackobj['toWard']).update(filledCount=genwardfilledcount - 1)
                    genunitfilledcount = General_UnitBedCount.objects.filter(unit = rollbackobj['fromUnit']).values()[0]['filledCount']
                    General_UnitBedCount.objects.filter(unit=rollbackobj['fromUnit']).update(filledCount=genunitfilledcount - 1)

                Patient.objects.filter(patientId = patId).update(loc = None,wardType = None,latestMovementId = None,admitted = False,
                                                                 admitDate = None,expAdmitDate = None)#isse check karo
                BedInfo.objects.filter(bedId = rollbackobj['toBedId']).update(currentUnit = None,status = 0,patientId = None)
                Movement.objects.filter(patientId=patId).delete()

            elif rollbackobj['toBedId'] is None: #discharge rollback
                if (rollbackobj['fromWardType'] == 'SPECIAL'):
                    spfilledcount = Special_UnitCount.objects.filter(unit=rollbackobj['fromUnit'],
                                                                     ward=rollbackobj['fromWard']).values()[0]['filledCount']
                    Special_UnitCount.objects.filter(unit=rollbackobj['fromUnit'],
                                                     ward=rollbackobj['fromWard']).update(filledCount=spfilledcount + 1)
                else:
                    genwardfilledcount = General_WardBedCount.objects.filter(ward=rollbackobj['fromWard']).values()[0]['filledCount']
                    General_WardBedCount.objects.filter(ward=rollbackobj['fromWard']).update(filledCount=genwardfilledcount + 1)
                    genunitfilledcount = General_UnitBedCount.objects.filter(unit=rollbackobj['fromUnit']).values()[0]['filledCount']
                    General_UnitBedCount.objects.filter(unit=rollbackobj['fromUnit']).update(filledCount=genunitfilledcount + 1)

                patientobj = Movement.objects.filter(patientId=patId).values().latest('Movementid')
                movementid = patientobj['Movementid']
                Movement.objects.filter(Movementid=movementid).update(outCondition=None, outDate=None)
                Patient.objects.filter(patientId=patId).update(loc=patientobj['ward'], wardType=patientobj['wardType'],
                                                               latestMovementId=movementid,admitted=True)  # isse check karo
                BedInfo.objects.filter(bedId=rollbackobj['fromBedId']).update(currentUnit=rollbackobj['fromUnit'], status=2,
                                                                                 patientId=patobj)

            else: #normal rollback
                if (rollbackobj['toWardType'] == 'SPECIAL'):
                    spfilledcount = Special_UnitCount.objects.filter(unit=rollbackobj['fromUnit'],
                                                                     ward=rollbackobj['toWard']).values()[0]['filledCount']
                    Special_UnitCount.objects.filter(unit=rollbackobj['fromUnit'],
                                                     ward=rollbackobj['toWard']).update(filledCount=spfilledcount - 1)
                else:
                    genwardfilledcount = General_WardBedCount.objects.filter(ward=rollbackobj['toWard']).values()[0]['filledCount']
                    General_WardBedCount.objects.filter(ward=rollbackobj['toWard']).update(filledCount=genwardfilledcount - 1)
                    genunitfilledcount = General_UnitBedCount.objects.filter(unit=rollbackobj['fromUnit']).values()[0]['filledCount']
                    General_UnitBedCount.objects.filter(unit=rollbackobj['fromUnit']).update(filledCount=genunitfilledcount - 1)

                if (rollbackobj['fromWardType'] == 'SPECIAL'):
                    spfilledcount = Special_UnitCount.objects.filter(unit=rollbackobj['fromUnit'],
                                                                     ward=rollbackobj['fromWard']).values()[0]['filledCount']
                    Special_UnitCount.objects.filter(unit=rollbackobj['fromUnit'],
                                                     ward=rollbackobj['fromWard']).update(filledCount=spfilledcount + 1)
                else:
                    genwardfilledcount = General_WardBedCount.objects.filter(ward=rollbackobj['fromWard']).values()[0]['filledCount']
                    General_WardBedCount.objects.filter(ward=rollbackobj['fromWard']).update(filledCount=genwardfilledcount + 1)
                    genunitfilledcount = General_UnitBedCount.objects.filter(unit=rollbackobj['fromUnit']).values()[0]['filledCount']
                    General_UnitBedCount.objects.filter(unit=rollbackobj['fromUnit']).update(filledCount=genunitfilledcount + 1)

                patientobj = Movement.objects.filter(patientId=patId).values().latest('Movementid')
                Movement.objects.filter(Movementid=patientobj['Movementid']).delete()
                patientobj = Movement.objects.filter(patientId=patId).values().latest('Movementid')
                movementid = patientobj['Movementid']
                Movement.objects.filter(Movementid=movementid).update(outCondition=None, outDate=None)
                Patient.objects.filter(patientId=patId).update(loc=patientobj['ward'], wardType=patientobj['wardType'],
                                                               latestMovementId=movementid,)  # isse check karo
                BedInfo.objects.filter(bedId=rollbackobj['fromBedId']).update(currentUnit=rollbackobj['fromUnit'],
                                                                                 status=2,
                                                                                 patientId=patobj)
                BedInfo.objects.filter(bedId=rollbackobj['toBedId']).update(currentUnit=None,status=0,patientId=None)
            k = {'status': 'rollback_successfull'}
            Rollback.objects.filter(patientId = patId).delete()
        k = json.dumps(k)
        return HttpResponse(k)






class MakeResponsible(APIView): ####code mein abhi aur change karna padega.........

    @transaction.atomic
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        employeeId = response['empId']

        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)


        empobj = Employee.objects.filter(empId = employeeId).values('unit','ward')[0]
        checkregobj = Employee.objects.filter(unit = empobj['unit'], ward = empobj['ward'], responsible = 1).values('empId')
        if len(checkregobj)==0:
            Employee.objects.filter(empId = employeeId).update(responsible = 1)
        else:
            Employee.objects.filter(empId=checkregobj[0]['empId']).update(responsible = 0)
            Employee.objects.filter(empId=employeeId).update(responsible=1)

        wardResidents = Employee.objects.filter(unit = empobj['unit'], ward = empobj['ward']).values('empId')

        for w in wardResidents:
            message_title = "Responsible resident changed"
            message_body = {'for': 'All', 'msg': "New Responsible resident of your ward is: " + employeeId}
            Messenger.sendMessage(w['empId'], message_title, message_body)

        r = {'status': 'Done'}

        r = json.dumps(r)
        return HttpResponse(r)

class ChangeWard(APIView):
    @transaction.atomic
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        employeeId = response['empId']
        deviceId = response['deviceId']

        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        newWard = response['changeWardTo']

        Employee.objects.filter(empId = employeeId).update(responsible = 0)

        Employee.objects.filter(empId = employeeId).update(ward = newWard)

        r = {'status' : "Done"}
        r = json.dumps(r)
        return HttpResponse(r)

class ForgetPassword(APIView):
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        employeeId = response['empId']
        dob = response['dob']

        obj = Employee.objects.filter(empId = employeeId,dob = dob).values()

        if len(obj)==0:
            r = {'status': "wrong dob",'error_msg':'wrong date entered'}
        else:
            r = {'status':'success'}
        r = json.dumps(r)
        return HttpResponse(r)

class SetPassword(APIView):

    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        employeeId = response['empId']
        newpass = response['newpass']
        Employee.objects.filter(empId = employeeId).update(password = newpass)
        r = {'status':'success'}
        r = json.dumps(r)
        return HttpResponse(r)


class FreeRetainedBeds(APIView): ### this function is used to free retained beds by previous patients, updated on 29 nov 2017

    @transaction.atomic
    def post(self, request):
        res = request.body.decode('utf-8')
        response = json.loads(res)

        deviceId = response['deviceId']
        currDeviceId = UserDeviceMapping.objects.filter(userId=response['empId']).values('deviceId')[0]
        if (currDeviceId['deviceId'] != deviceId):
            k = {'status': 'LOGOUT'}
            k = json.dumps(k)
            return HttpResponse(k)

        empId = response['empId']
        choice = response['choice'] ### choice means whether user wants to know patient on the bed or t0 free the bed
        ### choice = free means free the bed, choice = patient means get the patient retaining that bed
        empobj = Employee.objects.filter(empId=empId).values()[0]
        if empobj['empType'] == 'admin':
            bedid = response['bedId']
            bedobj = BedInfo.objects.filter(bedId = bedid).values()[0]
            if(bedobj['status']==3 ):
                if choice == 'free':
                    BedInfo.objects.filter(bedId = bedid).update(status=0,patientId = None,currentUnit = None)
                    bdobj = BedInfo.objects.filter(bedId = bedid).values()[0]

                    if (bdobj['wardType'] == 'SPECIAL'):
                        spfilledcount = Special_UnitCount.objects.filter(unit=bdobj['currentUnit'],
                                                        ward=bdobj['ward']).values()[0]['filledCount']
                        Special_UnitCount.objects.filter(unit=bdobj['currentUnit'],
                                                        ward=bdobj['ward']).update(filledCount=spfilledcount + 1)
                    else:
                        genwardfilledcount = General_WardBedCount.objects.filter(ward=bdobj['ward']).values()[0]['filledCount']
                        General_WardBedCount.objects.filter(ward=bdobj['ward']).update(filledCount=genwardfilledcount + 1)
                        genunitfilledcount = General_UnitBedCount.objects.filter(unit=bdobj['currentUnit']).values()[0]['filledCount']
                        General_UnitBedCount.objects.filter(unit=bdobj['currentUnit']).update(filledCount=genunitfilledcount + 1)

                    r = {'status': 'success', 'msg': 'Bed has been freed'}
                    r = json.dumps(r)
                    return HttpResponse(r)
                elif choice == 'patient':

                    patientId = bedobj['patientId_id']
                    patientobj = Patient.objects.filter(patientId = patientId).values()[0]
                    name = patientobj['name']

                    r = {'status': 'success', 'name': name, 'id':patientId}
                    r = json.dumps(r)
                    return HttpResponse(r)
            else:
                r = {'status':'err','err_msg':'sorry this bed is not retained bed'}
                r = json.dumps(r)
                return HttpResponse(r)
        else:
            r = {'status': 'err', 'err_msg': 'sorry you are not admin'}
            r = json.dumps(r)
            return HttpResponse(r)