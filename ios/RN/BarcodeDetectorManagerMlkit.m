#import "BarcodeDetectorManagerMlkit.h"
#import <React/RCTConvert.h>
#import "RNFileSystem.h"
#if __has_include(<MLKitBarcodeScanning/MLKitBarcodeScanning.h>)
@import MLKitVision;

@interface BarcodeDetectorManagerMlkit ()
@property(nonatomic, strong) MLKBarcodeScanner *barcodeRecognizer;
@property(nonatomic, assign) MLKBarcodeFormat setOption;
@property(nonatomic, assign) NSInteger detectionMode;
@property(nonatomic, assign) float scaleX;
@property(nonatomic, assign) float scaleY;
@end

@implementation BarcodeDetectorManagerMlkit

- (instancetype)init
{
  if (self = [super init]) {
    self.barcodeRecognizer = [MLKBarcodeScanner barcodeScanner];
  }
  return self;
}

- (BOOL)isRealDetector
{
  return true;
}

-(NSInteger)fetchDetectionMode
{
    return self.detectionMode;
}

+ (NSDictionary *)constants
{
    return @{
                @"CODE_128" : @(MLKBarcodeFormatCode128),
                @"CODE_39" : @(MLKBarcodeFormatCode39),
                @"CODE_93" : @(MLKBarcodeFormatCode93),
                @"CODABAR" : @(MLKBarcodeFormatCodaBar),
                @"EAN_13" : @(MLKBarcodeFormatEAN13),
                @"EAN_8" : @(MLKBarcodeFormatEAN8),
                @"ITF" : @(MLKBarcodeFormatITF),
                @"UPC_A" : @(MLKBarcodeFormatUPCA),
                @"UPC_E" : @(MLKBarcodeFormatUPCE),
                @"QR_CODE" : @(MLKBarcodeFormatQRCode),
                @"PDF417" : @(MLKBarcodeFormatPDF417),
                @"AZTEC" : @(MLKBarcodeFormatAztec),
                @"DATA_MATRIX" : @(MLKBarcodeFormatDataMatrix),
                @"ALL" : @(MLKBarcodeFormatAll),
            };
}

- (void)setType:(id)json queue:(dispatch_queue_t)sessionQueue
{
  NSInteger requestedValue = [RCTConvert NSInteger:json];
  if (self.setOption != requestedValue) {
      if (sessionQueue) {
          dispatch_async(sessionQueue, ^{
              self.setOption = requestedValue;
              MLKBarcodeScannerOptions *options =
              [[MLKBarcodeScannerOptions alloc]
              initWithFormats: requestedValue];
              self.barcodeRecognizer =
              [MLKBarcodeScanner barcodeScannerWithOptions:options];
          });
      }
  }
}

-(void)setMode:(id)json queue:(dispatch_queue_t)sessionQueue
{
    NSInteger requestedValue = [RCTConvert NSInteger:json];
    self.detectionMode = requestedValue;
}

- (void)findBarcodesInFrame:(UIImage *)uiImage
                  scaleX:(float)scaleX
                  scaleY:(float)scaleY
               completed:(void (^)(NSArray *result))completed
{
    self.scaleX = scaleX;
    self.scaleY = scaleY;
    MLKVisionImage *visionImage = [[MLKVisionImage alloc] initWithImage:uiImage];
    NSMutableArray *emptyResult = [[NSMutableArray alloc] init];
    [_barcodeRecognizer processImage:visionImage
        completion:^(NSArray<MLKBarcode *> *barcodes, NSError *error) {
            if (error != nil || barcodes == nil) {
                completed(emptyResult);
            } else {
                completed([self processBarcodes:barcodes imageContainingBarcodes:uiImage]);
            }
        }];
}

- (NSArray *)processBarcodes:(NSArray *)barcodes imageContainingBarcodes:(UIImage *)imageContainingBarcodes
{
    NSMutableArray *result = [[NSMutableArray alloc] init];
    for (MLKBarcode *barcode in barcodes) {
        NSMutableDictionary *resultDict =
        [[NSMutableDictionary alloc] initWithCapacity:20];
        // Boundaries of a barcode in image
        NSDictionary *bounds = [self processBounds:barcode.frame];
        [resultDict setObject:bounds forKey:@"bounds"];

        // TODO send points to javascript - implement on android at the same time
        // Point[] corners = barcode.getCornerPoints();

        NSString *rawValue = barcode.rawValue;
        NSString *displayValue = barcode.displayValue;
        [resultDict setObject:rawValue forKey:@"dataRaw"];
        [resultDict setObject:displayValue forKey:@"data"];

        // Store the image to app cache and return the uri
        NSString *path = [RNFileSystem generatePathInDirectory:[[RNFileSystem cacheDirectoryPath] stringByAppendingPathComponent:@"Camera"] withExtension:@".jpg"];
        [UIImageJPEGRepresentation(imageContainingBarcodes, 1.0) writeToFile:path atomically:YES];
        [resultDict setObject:path forKey:@"uri"];

        MLKBarcodeValueType valueType = barcode.valueType;
        [resultDict setObject:[self getType:barcode.valueType] forKey:@"type"];

        switch (valueType) {
            case MLKBarcodeValueTypeWiFi:
                if(barcode.wifi.ssid) {[resultDict setObject:barcode.wifi.ssid forKey:@"ssid"]; }
                if(barcode.wifi.password) {[resultDict setObject:barcode.wifi.password forKey:@"password"]; }
                if(barcode.wifi.type) {
                    NSString *encryptionTypeString = @"UNKNOWN";
                    int type = barcode.wifi.type;
                    switch (type) {
                        case MLKBarcodeWiFiEncryptionTypeWEP:
                            encryptionTypeString = @"WEP";
                            break;
                        case MLKBarcodeWiFiEncryptionTypeWPA:
                            encryptionTypeString = @"WPA";
                            break;
                        case MLKBarcodeWiFiEncryptionTypeOpen:
                            encryptionTypeString = @"Open";
                            break;
                        default:
                            break;
                    }
                    [resultDict setObject:encryptionTypeString forKey:@"encryptionType"];
                }
                break;
            case MLKBarcodeValueTypeURL:
                if(barcode.URL.url) { [resultDict setObject:barcode.URL.url forKey:@"url"]; }
                if(barcode.URL.title) { [resultDict setObject:barcode.URL.title forKey:@"title"]; }
                break;
            case MLKBarcodeValueTypeContactInfo:
                if(barcode.contactInfo.addresses) {
                    NSMutableArray *addresses = [[NSMutableArray alloc] init];
                    for (MLKBarcodeAddress *address in barcode.contactInfo.addresses) {
                        [addresses addObject:[self processAddress:address]];
                    }
                    [resultDict setObject:addresses forKey:@"addresses"];
                }
                if(barcode.contactInfo.emails) {
                    NSMutableArray *emails = [[NSMutableArray alloc] init];
                    for (MLKBarcodeEmail *email in barcode.contactInfo.emails) {
                        [emails addObject:[self processEmail:email]];
                    }
                    [resultDict setObject:emails forKey:@"emails"];
                }
                if(barcode.contactInfo.name) {
                    MLKBarcodePersonName *name = barcode.contactInfo.name;
                    NSObject *nameObject = @{
                                             @"formattedName" : name.formattedName ? name.formattedName : @"",
                                             @"firstName" : name.first ? name.first : @"",
                                             @"middleName" : name.middle ? name.middle : @"",
                                             @"lastName" : name.last ? name.last : @"",
                                             @"prefix" : name.prefix ? name.prefix : @"",
                                             @"pronounciation" : name.pronunciation ? name.pronunciation : @"",
                                             @"suffix" : name.suffix ? name.suffix : @"",
                                             };
                    [resultDict setObject:nameObject forKey:@"name"];
                }
                if(barcode.contactInfo.phones) {
                    NSMutableArray *phones = [[NSMutableArray alloc] init];
                    for (MLKBarcodePhone *phone in barcode.contactInfo.phones) {
                        [phones addObject:[self processPhone:phone]];
                    }
                    [resultDict setObject:phones forKey:@"phones"];
                }
                if(barcode.contactInfo.urls) {[resultDict setObject:barcode.contactInfo.urls forKey:@"urls"]; }
                if(barcode.contactInfo.organization) {[resultDict setObject:barcode.contactInfo.organization forKey:@"organization"]; }
                break;
            case MLKBarcodeValueTypeSMS:
                if(barcode.sms.message) {[resultDict setObject:barcode.sms.message forKey:@"message"]; }
                if(barcode.sms.phoneNumber) {[resultDict setObject:barcode.sms.phoneNumber forKey:@"phoneNumber"]; }
                break;
            case MLKBarcodeValueTypeGeographicCoordinates:
                if(barcode.geoPoint.latitude) {[resultDict setObject:@(barcode.geoPoint.latitude) forKey:@"latitude"]; }
                if(barcode.geoPoint.longitude) {[resultDict setObject:@(barcode.geoPoint.longitude) forKey:@"longitude"]; }
                break;
            case MLKBarcodeValueTypeDriversLicense:
                if(barcode.driverLicense.firstName) {[resultDict setObject:barcode.driverLicense.firstName forKey:@"firstName"]; }
                if(barcode.driverLicense.middleName) {[resultDict setObject:barcode.driverLicense.middleName forKey:@"middleName"]; }
                if(barcode.driverLicense.lastName) {[resultDict setObject:barcode.driverLicense.lastName forKey:@"lastName"]; }
                if(barcode.driverLicense.gender) {[resultDict setObject:barcode.driverLicense.gender forKey:@"gender"]; }
                if(barcode.driverLicense.addressCity) {[resultDict setObject:barcode.driverLicense.addressCity forKey:@"addressCity"]; }
                if(barcode.driverLicense.addressState) {[resultDict setObject:barcode.driverLicense.addressState forKey:@"addressState"]; }
                if(barcode.driverLicense.addressStreet) {[resultDict setObject:barcode.driverLicense.addressStreet forKey:@"addressStreet"]; }
                if(barcode.driverLicense.addressZip) {[resultDict setObject:barcode.driverLicense.addressZip forKey:@"addressZip"]; }
                if(barcode.driverLicense.birthDate) {[resultDict setObject:barcode.driverLicense.birthDate forKey:@"birthDate"]; }
                if(barcode.driverLicense.documentType) {[resultDict setObject:barcode.driverLicense.documentType forKey:@"documentType"]; }
                if(barcode.driverLicense.licenseNumber) {[resultDict setObject:barcode.driverLicense.licenseNumber forKey:@"licenseNumber"]; }
                if(barcode.driverLicense.expiryDate) {[resultDict setObject:barcode.driverLicense.expiryDate forKey:@"expiryDate"]; }
                if(barcode.driverLicense.issuingDate) {[resultDict setObject:barcode.driverLicense.issuingDate forKey:@"issuingDate"]; }
                if(barcode.driverLicense.issuingCountry) {[resultDict setObject:barcode.driverLicense.issuingCountry forKey:@"issuingCountry"]; }
                break;
            case MLKBarcodeValueTypeCalendarEvent:
                if(barcode.calendarEvent.eventDescription) {[resultDict setObject:barcode.calendarEvent.eventDescription forKey:@"eventDescription"]; }
                if(barcode.calendarEvent.location) {[resultDict setObject:barcode.calendarEvent.location forKey:@"location"]; }
                if(barcode.calendarEvent.organizer) {[resultDict setObject:barcode.calendarEvent.organizer forKey:@"organizer"]; }
                if(barcode.calendarEvent.status) {[resultDict setObject:barcode.calendarEvent.status forKey:@"status"]; }
                if(barcode.calendarEvent.summary) {[resultDict setObject:barcode.calendarEvent.summary forKey:@"summary"]; }
                if(barcode.calendarEvent.start) {
                    [resultDict setObject:[self processDate:barcode.calendarEvent.start] forKey:@"start"];
                }
                if(barcode.calendarEvent.end) {
                    [resultDict setObject:[self processDate:barcode.calendarEvent.end] forKey:@"end"];
                }
                break;
            case MLKBarcodeValueTypePhone:
                if(barcode.phone.number) {[resultDict setObject:barcode.phone.number forKey:@"number"]; }
                if(barcode.phone.type) {
                    [resultDict setObject:[self getPhoneType:barcode.phone.type] forKey:@"phoneType"];
                }
                break;
            case MLKBarcodeValueTypeEmail:
                if(barcode.email.address) {[resultDict setObject:barcode.email.address forKey:@"address"]; }
                if(barcode.email.body) {[resultDict setObject:barcode.email.body forKey:@"body"]; }
                if(barcode.email.subject) {[resultDict setObject:barcode.email.subject forKey:@"subject"]; }
                if(barcode.email.type) {[resultDict setObject:[self getEmailType:barcode.email.type] forKey:@"emailType"]; }
                break;
            default:
                break;
        }
        [result addObject:resultDict];
    }
    return result;
}

- (NSString *)getType:(int)type
{
    NSString *barcodeType = @"UNKNOWN";
    switch (type) {
        case MLKBarcodeValueTypeEmail:
            barcodeType = @"EMAIL";
            break;
        case MLKBarcodeValueTypePhone:
            barcodeType = @"PHONE";
            break;
        case MLKBarcodeValueTypeCalendarEvent:
            barcodeType = @"CALENDAR_EVENT";
            break;
        case MLKBarcodeValueTypeDriversLicense:
            barcodeType = @"DRIVER_LICENSE";
            break;
        case MLKBarcodeValueTypeGeographicCoordinates:
            barcodeType = @"GEO";
            break;
        case MLKBarcodeValueTypeSMS:
            barcodeType = @"SMS";
            break;
        case MLKBarcodeValueTypeContactInfo:
            barcodeType = @"CONTACT_INFO";
            break;
        case MLKBarcodeValueTypeWiFi:
            barcodeType = @"WIFI";
            break;
        case MLKBarcodeValueTypeText:
            barcodeType = @"TEXT";
            break;
        case MLKBarcodeValueTypeISBN:
            barcodeType = @"ISBN";
            break;
        case MLKBarcodeValueTypeProduct:
            barcodeType = @"PRODUCT";
            break;
        default:
            break;
    }
    return barcodeType;
}

- (NSString *)getPhoneType:(int)type
{
    NSString *typeString = @"UNKNOWN";
    switch (type) {
        case MLKBarcodePhoneTypeFax:
            typeString = @"Fax";
            break;
        case MLKBarcodePhoneTypeHome:
            typeString = @"Home";
        case MLKBarcodePhoneTypeWork:
            typeString = @"Work";
        case MLKBarcodePhoneTypeMobile:
            typeString = @"Mobile";
        default:
            break;
    }
    return typeString;
}

- (NSString *)getEmailType:(int)type
{
    NSString *typeString = @"UNKNOWN";
    switch (type) {
        case MLKBarcodeEmailTypeWork:
            typeString = @"Work";
            break;
        case MLKBarcodeEmailTypeHome:
            typeString = @"Home";
        default:
            break;
    }
    return typeString;
}

- (NSDictionary *)processPhone:(MLKBarcodePhone *)phone
{
    NSString *number = @"";
    NSString *typeString = @"UNKNOWN";
    if (phone) {
        typeString = [self getPhoneType:phone.type];
        number = phone.number;
    }
    return @{@"number" : number, @"phoneType" : typeString};
}

- (NSDictionary *)processAddress:(MLKBarcodeAddress *)address
{
    NSArray *addressLines = [[NSArray alloc] init];
    NSString *typeString = @"UNKNOWN";
    if (address) {
        int type = address.type;
        NSString *typeString = @"UNKNOWN";
        switch (type) {
            case MLKBarcodeAddressTypeWork:
                typeString = @"Work";
                break;
            case MLKBarcodeAddressTypeHome:
                typeString = @"Home";
            default:
                break;
        }
        addressLines = address.addressLines;
    }
    return @{@"addressLines" : addressLines, @"addressType" : typeString};
}

- (NSDictionary *)processEmail:(MLKBarcodeEmail *)email
{
    NSString *subject = @"";
    NSString *address  =@"";
    NSString *body  =@"";
    NSString *typeString = @"UNKNOWN";
    if (email) {
        if (email.subject) { subject = email.subject; }
        if (email.address) { address = email.address; }
        if (email.body) { body = email.body; }
        typeString = [self getEmailType:email.type];
    }
    return @{@"subject" : subject, @"body" : body, @"address" : address, @"emailType" : typeString};
}

- (NSString *)processDate:(NSDate *)date
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"dd-MM-YYYY HH:mm:ss"];
    return [dateFormatter stringFromDate:date];
}

- (NSDictionary *)processBounds:(CGRect)bounds
{
    float width = bounds.size.width * _scaleX;
    float height = bounds.size.height * _scaleY;
    float originX = bounds.origin.x * _scaleX;
    float originY = bounds.origin.y * _scaleY;
    NSDictionary *boundsDict = @{
                                 @"size" : @{@"width" : @(width), @"height" : @(height)},
                                 @"origin" : @{@"x" : @(originX), @"y" : @(originY)}
                                 };
    return boundsDict;
}


- (NSDictionary *)processPoint:(MLKVisionPoint *)point
{
    float originX = point.x * _scaleX;
    float originY = point.y * _scaleY;
    NSDictionary *pointDict = @{
                                @"x" : @(originX),
                                @"y" : @(originY)
                                };
    return pointDict;
}

@end
#else

@interface BarcodeDetectorManagerMlkit ()
@end

@implementation BarcodeDetectorManagerMlkit

- (instancetype)init {
    self = [super init];
    return self;
}

- (BOOL)isRealDetector {
    return false;
}

- (void)findBarcodesInFrame:(UIImage *)image
                       scaleX:(float)scaleX
                       scaleY:(float)scaleY
                       completed:(void (^)(NSArray *result))completed;
{
    NSLog(@"BarcodeDetector not installed, stub used!");
    NSArray *barcodes = @[ @"Error, Barcode Detector not installed" ];
    completed(barcodes);
}

+ (NSDictionary *)constants
{
    return @{
             @"CODE_128" : @{},
             @"CODE_39" : @{},
             @"CODE_93" : @{},
             @"CODABAR" : @{},
             @"EAN_13" : @{},
             @"EAN_8" : @{},
             @"ITF" : @{},
             @"UPC_A" : @{},
             @"UPC_E" : @{},
             @"QR_CODE" : @{},
             @"PDF417" : @{},
             @"AZTEC" : @{},
             @"DATA_MATRIX" : @{},
             };
}

- (void)setType:(id)json queue:(dispatch_queue_t)sessionQueue
{
    return;
}

@end
#endif

