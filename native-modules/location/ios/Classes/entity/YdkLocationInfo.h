//
//  YdkLocationInfo.h
//  ydk-location
//
//  Created by yryz on 2019/7/25.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@interface YdkLocationInfo : NSObject

@property (nonatomic) CLLocationCoordinate2D coordinate;

@property (nonatomic, copy) NSString *province;
@property (nonatomic, copy) NSString *city;
@property (nonatomic, copy) NSString *region;
@property (nonatomic, copy) NSString *address;

@property (nonatomic, copy) NSString *citycode;
@property (nonatomic, copy) NSString *adcode;
@property (nonatomic, copy) NSString *name;

- (instancetype)initWithCLLocationCoordinate2D:(CLLocationCoordinate2D)coordinate province:(NSString *)province city:(NSString *)city region:(NSString *)region address:(NSString *)address citycode:(NSString *)citycode adcode:(NSString *)adcode name:(NSString *)name;

- (instancetype)initWithCLLocationCoordinate2D:(CLLocationCoordinate2D)coordinate;

- (instancetype)initWithCLLocationCoordinate2D:(CLLocationCoordinate2D)coordinate province:(NSString *)province city:(NSString *)city region:(NSString *)region address:(NSString *)address;

@end
