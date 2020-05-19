//
//  YdkLocation.m
//  ydk-location
//
//  Created by yryz on 2019/7/25.
//

#import "YdkLocation.h"
#import "YdkLocationInfo.h"

#import <ydk-permission/YdkPermissionManager.h>
#import <ydk-core/YdkCore.h>
#import <AMapFoundationKit/AMapFoundationKit.h>
#import <AMapLocationKit/AMapLocationKit.h>

NSErrorDomain const YdkLocationErrorDomain = @"YdkLocationErrorDomain";

@interface YdkLocation () <AMapLocationManagerDelegate>

//@property (nonatomic ,strong) CLLocationManager *locationManager;
//@property (nonatomic,strong) CLGeocoder *geocoder;

@property (nonatomic, strong) AMapLocationManager *locationManager;

@property (nonatomic, strong) NSMutableArray<RACSubject *> *requestToRetry;
@property (nonatomic, strong) NSLock *lock;
@property (atomic, assign) BOOL isRequest;

@end

@implementation YdkLocation {
    NSString *_appKey;
}

+ (void)load {
    ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config {
    if (self = [super init]) {
        _appKey = [config valueForKeyPath:@"map.appKey"];
        _requestToRetry = [NSMutableArray array];
        _lock = [NSLock new];
    }
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [AMapServices sharedServices].apiKey = _appKey;
    });
    return YES;
}

- (AMapLocationManager *)locationManager {
    if (!_locationManager) {
        _locationManager = [[AMapLocationManager alloc] init];
        _locationManager.delegate = self;
        _locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters;
        _locationManager.distanceFilter = 100.0f;
        // _locationManager.locatingWithReGeocode = YES;
        // _locationManager.locationTimeout = 15;
        // _locationManager.reGeocodeTimeout = 15;
    }
    return _locationManager;
}

+ (RACSignal<YdkLocationInfo *> *)requestLocation {
    YdkLocation *location = ydk_get_module_instance(self.class);
    return [location requestLocation];
}

- (RACSignal<YdkLocationInfo *> *)requestLocation {
    
    RACSubject *subject = [RACSubject subject];
    [self.lock lock];
    [self.requestToRetry addObject:subject];
    [self.lock unlock];
    
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(1);
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    if (!self.isRequest) {
        self.isRequest = YES;
        dispatch_semaphore_signal(semaphore);
        dispatch_async(dispatch_get_main_queue(), ^{
            @weakify(self);
            [[self _requestLocation] subscribeNext:^(id x) {
                @strongify(self);
                [self.lock lock];
                self.isRequest = NO;
                for (RACSubject *subject in self.requestToRetry) {
                    [subject sendNext:x];
                    [subject sendCompleted];
                }
                [self.requestToRetry removeAllObjects];
                [self.lock unlock];
            } error:^(NSError * error) {
                @strongify(self);
                [self.lock lock];
                self.isRequest = NO;
                for (RACSubject *subject in self.requestToRetry) {
                    [subject sendError:error];
                }
                [self.requestToRetry removeAllObjects];
                [self.lock unlock];
            }];
        });
    } else {
        dispatch_semaphore_signal(semaphore);
    }
    return subject;
}

- (RACSignal<YdkLocationInfo *> *)_requestLocation {
    return [[YdkPermissionManager requestAuthorization:YdkPermissionAuthorizationTypeLocationWhenInUse]
            flattenMap:^RACSignal *(NSNumber *value) {
        YdkPermissionAuthorizationStatus status = [value integerValue];
        return [self _requestLocation:status];
    }];
}

- (RACSignal<YdkLocationInfo *> *)_requestLocation:(YdkPermissionAuthorizationStatus)status {
    if (status != YdkPermissionAuthorizationStatusAuthorized ) {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            NSError *error = [NSError errorWithDomain:YdkLocationErrorDomain code:YdkLocationErrorAuthorizationDenied userInfo:@{NSLocalizedDescriptionKey : @"没有获取位置信息权限"}];
            [subscriber sendError:error];
            return nil;
        }];
    } else {
        @weakify(self);
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            @strongify(self);
            
            @weakify(self);
            BOOL result = [self.locationManager requestLocationWithReGeocode:YES completionBlock:^(CLLocation *location, AMapLocationReGeocode *regeocode, NSError *error) {
                @strongify(self);
                if (error) {
                    [subscriber sendError:error];
                } else {
                    YdkLocationInfo *info = [self locationInfoWithLocation:location regeocode:regeocode];
                    [subscriber sendNext:info];
                    [subscriber sendCompleted];
                }
            }];
            if (!result) {
                NSError *error = [NSError errorWithDomain:YdkLocationErrorDomain code:YdkLocationErrorLocationRequestFailed userInfo:@{ NSLocalizedDescriptionKey : @"定位请求添加失败" }];
                // [self.locationManager stopUpdatingHeading];
                [subscriber sendError:error];
            }
            return nil;
        }];
    }
}

//- (CLLocationManager *)locationManager {
//    if (!_locationManager) {
//        _locationManager = [[CLLocationManager alloc] init];
//        _locationManager.delegate = self;
//        _locationManager.desiredAccuracy = kCLLocationAccuracyBest;
//        _locationManager.distanceFilter = 10.0f;
//    }
//    return _locationManager;
//}
//
//- (CLGeocoder *)geocoder {
//    if (!_geocoder) {
//        _geocoder = [[CLGeocoder alloc] init];
//    }
//    return _geocoder;
//}

// MARK: - AMapLocationManagerDelegate
//- (void)amapLocationManager:(AMapLocationManager *)manager didFailWithError:(NSError *)error {
//    [manager stopUpdatingLocation];
//    [_subject sendError:error];
//}
//
//- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location reGeocode:(AMapLocationReGeocode *)reGeocode {
//    if (!reGeocode) {
//        return;
//    }
//    [manager stopUpdatingLocation];
//    NSString *adcode = reGeocode.adcode;
//    if (reGeocode.adcode && [reGeocode.adcode isKindOfClass:[NSString class]] && reGeocode.adcode.length > 0) {
//        // adcode = [NSString stringWithFormat:@"%@00", [adcode substringToIndex:4]];
//        NSInteger length = 12 - adcode.length;
//        if (length > 0) {
//            NSString *complement = [NSString stringWithFormat:@"%012lld", adcode.longLongValue];
//            adcode = [NSString stringWithFormat:@"%@%@", adcode, [complement substringToIndex:length]];
//        }
//    }
//    YdkLocationInfo *info = [[YdkLocationInfo alloc]
//                             initWithCLLocationCoordinate2D:location.coordinate
//                             province:reGeocode.province
//                             city:reGeocode.city
//                             region:reGeocode.district
//                             address:reGeocode.formattedAddress
//                             citycode:reGeocode.citycode
//                             adcode:adcode
//                             name: reGeocode.POIName];
//    [_subject sendNext:info];
//    [_subject sendCompleted];
//}
/**
// MARK: - CLLocationManagerDelegate
- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
//    [_locationManager stopUpdatingLocation];
    [_subject sendError:error];
}

-(void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
//    [_locationManager stopUpdatingLocation];
    CLLocation *location = locations.lastObject;

    @weakify(self);
    // 反地理编码
    [self.geocoder reverseGeocodeLocation:location completionHandler:^(NSArray<CLPlacemark *> *placemarks, NSError *error) {
        @strongify(self);
        if (error) {
            [self.subject sendError:error];
        } else {
            CLLocationCoordinate2D coordinate = [self transformCoordinate:location.coordinate];
            YdkLocationInfo *info = [[YdkLocationInfo alloc] initWithCLLocationCoordinate2D:coordinate];
            if (placemarks.count > 0) {
                CLPlacemark *placemark = [placemarks firstObject];
                info.province = placemark.administrativeArea;
                info.city = placemark.locality;
                info.region = placemark.subLocality;
                NSMutableString *address = [NSMutableString string];
                if (placemark.thoroughfare){
                    [address appendString:placemark.thoroughfare];
                }
                if (placemark.subThoroughfare) {
                    [address appendString:placemark.subThoroughfare];
                }
                info.address = address;
            }
            [self.subject sendNext:info];
            [self.subject sendCompleted];
        }
    }];
}

// MARK: - transform
static const double a = 6378245.0;
static const double ee = 0.00669342162296594323;

- (BOOL)isChinaAreaWithCoordinate:(CLLocationCoordinate2D)coordinate {
    // 纬度3.86~53.55,经度73.66~135.05
    return !(coordinate.longitude > 73.66 && coordinate.longitude < 135.05 && coordinate.latitude > 3.86 && coordinate.latitude < 53.55);
}

- (CLLocationCoordinate2D)originCoordinate:(CLLocationCoordinate2D)coordinate {
    
    CLLocationDegrees lat = coordinate.latitude;
    CLLocationDegrees lng = coordinate.longitude;
    // lat
    CLLocationDegrees dLat = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * sqrt(fabs(lng));
    dLat += (20.0 * sin(6.0 * lng * M_PI) + 20.0 * sin(2.0 * lng * M_PI)) * 2.0 / 3.0;
    dLat += (20.0 * sin(lat * M_PI) + 40.0 * sin(lat / 3.0 * M_PI)) * 2.0 / 3.0;
    dLat += (160.0 * sin(lat / 12.0 * M_PI) + 320 * sin(lat * M_PI / 30.0)) * 2.0 / 3.0;
    
    // lng
    CLLocationDegrees dLng = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * sqrt(fabs(lng));
    dLng += (20.0 * sin(6.0 * lng * M_PI) + 20.0 * sin(2.0 * lng * M_PI)) * 2.0 / 3.0;
    dLng += (20.0 * sin(lng * M_PI) + 40.0 * sin(lng / 3.0 * M_PI)) * 2.0 / 3.0;
    dLng += (150.0 * sin(lng / 12.0 * M_PI) + 300.0 * sin(lng / 30.0 * M_PI)) * 2.0 / 3.0;
    
    return CLLocationCoordinate2DMake(dLat, dLng);
}

// wgs84 -> gcj02
- (CLLocationCoordinate2D)transformCoordinate:(CLLocationCoordinate2D)coordinate {
    CLLocationDegrees lat = coordinate.latitude;
    CLLocationDegrees lng = coordinate.longitude;
    
    coordinate.latitude -= 35.0;
    coordinate.longitude -= 105.0;
    CLLocationCoordinate2D originCoordinate = [self originCoordinate:coordinate];
    
    CLLocationDegrees dlat = originCoordinate.latitude;
    CLLocationDegrees dlng = originCoordinate.longitude;
    double radlat = lat / 180.0 * M_PI;
    double magic = sin(radlat);
    magic = 1 - ee * magic * magic;
    double sqrtmagic = sqrt(magic);
    dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * M_PI);
    dlng = (dlng * 180.0) / (a / sqrtmagic * cos(radlat) * M_PI);
    double mglat = lat + dlat;
    double mglng = lng + dlng;
    return CLLocationCoordinate2DMake(mglat, mglng);
}
*/

- (YdkLocation *)locationInfoWithLocation:(CLLocation *)location regeocode:(AMapLocationReGeocode *)regeocode {
    NSString *adcode = regeocode.adcode;
    if (regeocode.adcode && [regeocode.adcode isKindOfClass:[NSString class]] && regeocode.adcode.length > 0) {
        // adcode = [NSString stringWithFormat:@"%@00", [adcode substringToIndex:4]];
        NSInteger length = 12 - adcode.length;
        if (length > 0) {
            NSString *complement = [NSString stringWithFormat:@"%012lld", adcode.longLongValue];
            adcode = [NSString stringWithFormat:@"%@%@", adcode, [complement substringToIndex:length]];
        }
    }
    
    YdkLocationInfo *info = [[YdkLocationInfo alloc]
                             initWithCLLocationCoordinate2D:location.coordinate
                             province:regeocode.province
                             city:regeocode.city
                             region:regeocode.district
                             address:regeocode.formattedAddress
                             citycode:regeocode.citycode
                             adcode:adcode
                             name: regeocode.POIName];
    return info;
}

- (void)dealloc {
//    [_locationManager stopUpdatingLocation];
    _locationManager.delegate = nil;
}

@end
