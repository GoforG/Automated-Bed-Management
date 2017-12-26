from django.db import models

# Create your models here.
#name, depID
class UserDeviceMapping(models.Model):
    userId = models.CharField(max_length=30, primary_key=True)
    deviceId = models.CharField(max_length=100,null = True)
    def __str__(self):
            return self.userId


class Employee(models.Model):

    empId = models.CharField(max_length=30, primary_key=True)
    password = models.CharField(max_length=30)
    dob = models.CharField(max_length=8,null=True)
    name = models.CharField(max_length=30)
    depid = models.CharField(max_length=30)
    designationid = models.CharField(max_length=30)
    ward =  models.CharField(max_length=30,null=True)
    empType = models.CharField(max_length=30,null= True)
    unit = models.CharField(max_length=30)
    responsiblity = ((0,'not responsible'),(1,'responsible'))
    responsible = models.IntegerField(choices=responsiblity,default='not responsible')
    
    def __str__(self):
        return self.empId
    
class Movement(models.Model):
    Movementid = models.AutoField(primary_key=True)
    patientId = models.CharField(max_length=30)
    ward = models.CharField(max_length=30)
    wardType = models.CharField(max_length=30)
    inDate = models.DateField(auto_now_add=True)
    outDate = models.DateField(null = True)
    inCondition = models.IntegerField(null=True)
    outCondition = models.IntegerField(null = True)
    inExpectedDaysInWard = models.DateField(null=True)
    inExpectedDaysInHospital = models.DateField(null=True)
    #retainstatus = models.CharField(max_length=10, default='NO')

    def __str__(self):
        return str(self.Movementid)

class Rollback(models.Model):

    patientId = models.CharField(max_length=30)
    fromWard = models.CharField(max_length=30, null=True)
    toWard = models.CharField(max_length=30, null=True)
    fromWardType = models.CharField(max_length=30, null=True)
    toWardType = models.CharField(max_length=30, null=True)
    fromBedId = models.CharField(max_length=30, null=True)
    toBedId = models.CharField(max_length=30, null=True)
    timestamp = models.DateTimeField(auto_now_add=True)
    fromUnit = models.CharField(max_length=30, null=True)

    def __str__(self):
        return str(self.id)



                # name, sex,
class Patient(models.Model):
    patientId = models.CharField(max_length=30, primary_key=True)
    name = models.CharField(max_length=30,null = True)
    age = models.PositiveIntegerField(null = True)
    sextype = (('M','male'),('F','female'))
    sex = models.CharField(choices  = sextype,max_length = 2,null = True)
    #currCond = models.IntegerField(max_length=30)
    unit = models.CharField(max_length=30,null = True)
    depid = models.CharField(max_length=30,null = True)
    loc = models.CharField(max_length=30,null = True,blank = True)
    wardType = models.CharField(max_length=30,null = True,blank = True)
    diagnosis = models.CharField(max_length=30,null = True)
    pincode =  models.IntegerField(null = True)

    #don't use latestMovementId. It can be get by simple querying also.
    latestMovementId = models.ForeignKey(Movement,null=True,blank = True, on_delete=models.SET_NULL)

    contactNo = models.IntegerField(null = True)
    #email = models.EmailField(null=True,blank=True)
    admitted = models.BooleanField(default=False)
    regDate = models.DateField(null = True)
    expAdmitDate = models.DateField(null = True,blank = True)
    admitDate = models.DateField(null = True,blank = True)

    def __str__(self):
        return self.patientId

### Bed Info of all general beds that are part of common pool among all wards and it also includes info of special beds among the wards which are not part
### of common pool
class BedInfo(models.Model):
    bedId = models.CharField(max_length=30, primary_key=True)
    ward = models.CharField(max_length=100)
    wardType = models.CharField(max_length=30)
    depid = models.CharField(max_length=30)
 #   unit = models.CharField(max_length=30, null=True)
    currentUnit = models.CharField(max_length=30, null=True,blank = True)#### Should be initially same as unit (avalibility to be cheked based on this info only)
    statustype = ((0,'notbooked'),(1,'partialbooked'),(2,'booked'),(3,'retained'))
    status = models.IntegerField(choices = statustype,default = 'notbooked')
    patientId = models.ForeignKey(Patient,null=True,blank=True)

    def __str__(self):
        return self.bedId



### It gives count of general beds in all the wards and also tells how many of them are filled
class General_WardBedCount(models.Model):
    ward = models.CharField(max_length=100)
    depid = models.CharField(max_length=50)
    totalCount = models.IntegerField()
    filledCount = models.IntegerField()

    def __str__(self):
        return self.ward

### It gives the threshold limit for beds for each unit of a particular department and how many of them are filled
class General_UnitBedCount(models.Model):
    unit = models.CharField(max_length=100)
    depid = models.CharField(max_length=50)
    threshold = models.IntegerField()
    filledCount = models.IntegerField()

    def __str__(self):
        return self.unit

### It gives info about bed is taken from which unit when threshold limit of a unit is crossed
class Borrowed_Beds(models.Model):
    #bedId = models.CharField(max_length=100)
    borrowingid = models.AutoField(primary_key=True)
    depId = models.CharField(max_length=50, null = True)
    fromUnit = models.CharField(max_length=50)
    toUnit = models.CharField(max_length=50)
    timestamp = models.DateField(null = True,blank = True)
    def __str__(self):
        return str(self.borrowingid)

### It gives the filled and total count of the beds in the wards of special type
class Special_UnitCount(models.Model):
    unit = models.CharField(max_length=100)
    ward = models.CharField(max_length=100)
    filledCount = models.IntegerField()
    totalCount = models.IntegerField()
    depid = models.CharField(max_length=50)

    def __str__(self):
        return self.unit


class TransferTable(models.Model): #for resident
    patientId = models.ForeignKey(Patient,null=False, unique=True)
    name = models.CharField(max_length=30)
    fromUnit = models.CharField(max_length=30,null=True)
    #toUnit = models.CharField(max_length=30, null=True) ####
    fromWard = models.CharField(max_length=30,null=True)
    toWard = models.CharField(max_length=30, null=True)
    fromWardType = models.CharField(max_length=30, null=True)
    toWardType = models.CharField(max_length=30, null=True)
    fromBedId = models.CharField(max_length=30,null=True)
    toBedId = models.CharField(max_length=30,null=True)
    timestamp = models.DateTimeField(auto_now_add=True)
    condition = models.IntegerField(default=60)#apne mann se
    daysexpected = models.DateField()
    lastverifiedward = models.CharField(max_length=30,null=True)
    retainstatus = models.CharField(max_length=10, default='NO')
    ###we need to add to resident so that when nurse confirms transfer, we can send notification to that resident as well

    def __str__(self):
        return self.name


class Request(models.Model):
    requestid = models.AutoField(primary_key=True)
    issuing_doctor_name=models.CharField(max_length=30)
    patientId = models.CharField(max_length=30, null=False)
    patient_name = models.CharField(max_length=30)
    condition = models.IntegerField()#
    daysexpected = models.DateField()#
    from_resident=models.CharField(max_length=30)
    #to_resident=models.CharField(max_length=30)
    fromUnit = models.CharField(max_length=30, null=True)
    toUnit = models.CharField(max_length=30, null=True) ####
    fromWard = models.CharField(max_length=30, null=True)
    toWard = models.CharField(max_length=30)
    fromWardType = models.CharField(max_length=30, null=True)
    toWardType = models.CharField(max_length=30, null=True)
    fromBedId = models.CharField(max_length=30, null=True)
    #toBedId = models.CharField(max_length=30, null=True)
    returnbedId = models.CharField(max_length=30, null=True)
    status = ((0, 'Pending'), (1, 'Approved'), (2, 'Rejected'), (3, 'Waiting For Borrowing Response'))
    approval_status = models.IntegerField(choices=status, default=0)
    from_msg = models.CharField(max_length=200, null=True)
    to_msg = models.CharField(max_length=200, null=True)
    req_type = ((0, 'IntraUnit'), (1, 'InterUnit'))####
    requestType = models.IntegerField(choices=req_type, default=0)####
    timestamp = models.DateTimeField(auto_now_add=True)
    retainstatus = models.CharField(max_length=10, default='NO')

    def __str__(self):
            return self.from_resident



class Checkup(models.Model):
    patientmovementid = models.ForeignKey(Movement,null=False)
    patientId = models.ForeignKey(Patient,null=False,blank=False)
    bedId = models.CharField(max_length=30)
    timestamp = models.DateTimeField(auto_now_add=True)
    condition = models.IntegerField()
    expDischargefromhosp = models.DateField(null = True, blank = True)
    expDischargefromward = models.DateField(null = True, blank = True)

    def __str__(self):
        return str(self.id)

class PatientVisit(models.Model):
    patientId = models.ForeignKey(Patient,null=False,blank=False)
    currCond = models.IntegerField()
    timestamp = models.DateTimeField(auto_now_add=True)
    expectedAdmitDate = models.DateField()
    def __str__(self):
        return str(self.id)

#class BorrowedBeds(models.Model):
#    bedId = models.CharField(max_length=30)
#    borrower_residentId = models.CharField(max_length=30)
#    fromUnit = models.CharField(max_length=30)
#    toUnit = models.CharField(max_length=30, null=True)
#    fromWard = models.CharField(max_length=30, null=True)
#    toWard = models.CharField(max_length=30, null=True)
#    timestamp = models.DateTimeField(auto_now_add=True)
#    def __str__(self):
#        return self.bedId

    
    
    
