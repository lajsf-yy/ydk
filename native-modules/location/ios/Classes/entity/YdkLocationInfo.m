//
//  YdkLocationInfo.m
//  ydk-location
//
//  Created by yryz on 2019/7/25.
//

#import "YdkLocationInfo.h"

@implementation YdkLocationInfo

- (instancetype)initWithCLLocationCoordinate2D:(CLLocationCoordinate2D)coordinate {
    return [self initWithCLLocationCoordinate2D:coordinate province:nil city:nil region:nil address:nil];
}

- (instancetype)initWithCLLocationCoordinate2D:(CLLocationCoordinate2D)coordinate province:(NSString *)province city:(NSString *)city region:(NSString *)region address:(NSString *)address {
    return [self initWithCLLocationCoordinate2D:coordinate province:province city:city region:region address:nil citycode:nil adcode:nil name:nil];
}

- (instancetype)initWithCLLocationCoordinate2D:(CLLocationCoordinate2D)coordinate province:(NSString *)province city:(NSString *)city region:(NSString *)region address:(NSString *)address citycode:(NSString *)citycode adcode:(NSString *)adcode name:(NSString *)name {
    if (self = [super init]) {
        _coordinate = coordinate;
        _province = [province copy];
        _city = [city copy];
        _region = [region copy];
        _address = [address copy];
        _citycode = [citycode copy];
        _adcode = [adcode copy];
        _name = [name copy];
    }
    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"(%.0f, %.0f) %@ %@ %@ %@", _coordinate.latitude, _coordinate.longitude, _province, _city, _region, _address];
}

@end
