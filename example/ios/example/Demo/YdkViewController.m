//
//  YdkViewController.m
//  example
//
//  Created by yryz on 2019/6/24.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "YdkViewController.h"
#import "VideoViewController.h"

#import <ydk-network/YdkNetwork.h>
#import <ydk-toolkit/YdkToolkit.h>
#import <ydk-permission/YdkPermissionManager.h>
#import <ydk-location/YdkLocation.h>
#import <SDWebImage/SDImageCodersManager.h>
#import <SDWebImageWebPCoder/SDImageWebPCoder.h>
#import <SDWebImage/UIImageView+WebCache.h>

#import <HealthKit/HealthKit.h>

#import <ydk-toolkit/YdkToolkit.h>
#import <ydk-album/YdkAlbum.h>
#import <ydk-trimmer/YdkVideoTrimmerViewController.h>

#import <YYModel/YYModel.h>

@interface YdkViewController () <YdkNetworkInterceptorProtocol>

@end

@implementation YdkViewController

- (void)viewDidLoad {
  [super viewDidLoad];
}

- (IBAction)testNetwork:(id)sender {
    NSDictionary *params = @{ @"pageNo" : @1, @"pageSize" : @20, @"circleId" : @519392279527424 };
  [[YdkNetwork GET:@"cms" URLString:@"/pb/community-posts/action/list-circle" parameters:params] subscribeNext:^(id  _Nullable x) {
      DLog(@"reponseObject:%@", x);
  } error:^(NSError * _Nullable error) {
      
  }];
}

- (YdkRequest *)requestWithIncompleteRequest:(YdkRequest *)request {
  [request.allHTTPHeaderFields setObject:@"346167614259200" forKey:@"userId"];
  [request.allHTTPHeaderFields setObject:@"1" forKey:@"devType"];
  [request.allHTTPHeaderFields setObject:@"lovelorn" forKey:@"tenantId"];
  [request.allHTTPHeaderFields setObject:@"1.4.0" forKey:@"appVersion"];
  [request.allHTTPHeaderFields setObject:@"SLAPP" forKey:@"ditchCode"];
  [request.allHTTPHeaderFields setObject:@"" forKey:@"devId"];
  [request.allHTTPHeaderFields setObject:@"192.168.30.188" forKey:@"ip"];
  [request.allHTTPHeaderFields setObject:@"MRp2889rlVgf1560738662237"  forKey:@"token"];
  return request;
}

- (RACSignal<id> *)handleErrorResponse:(NSInteger)errCode {
  return nil;
}

- (IBAction)testAlert:(id)sender {
  YdkAlertView *alert = [YdkAlertView alertWithTitle:nil message:@"你与服务器连接丢失，点击重新连接重试" preferredStyle:YdkAlertStyleAlert];
  [alert.defaultContainer.layer insertSublayer:[self gradientLayer:alert.defaultContainer] atIndex:0];
//  UIView *aView = [[NSBundle mainBundle] loadNibNamed:@"View" owner:nil options:nil].firstObject;
//  YdkAlertView *alert = [[YdkAlertView alloc] initWithContentView:aView preferredStyle:YdkAlertStyleActionSheet];
  YdkAlertAction *cancel = [YdkAlertAction actionWithTitle:@"不需要" style:YdkAlertActionStyleCancel handler:nil];
  [alert addAction:cancel];
  [alert show];
}

- (CALayer *)gradientLayer:(UIView *)superview {
  CAGradientLayer *gradientLayer0 = [[CAGradientLayer alloc] init];
  gradientLayer0.cornerRadius = superview.height / 2.0;
  gradientLayer0.frame = superview.bounds;
  gradientLayer0.colors = @[(id)[UIColor colorWithRed:222.0f/255.0f green:36.0f/255.0f blue:208.0f/255.0f alpha:1.0f].CGColor,
                            (id)[UIColor colorWithRed:254.0f/255.0f green:94.0f/255.0f blue:142.0f/255.0f alpha:1.0f].CGColor];
  gradientLayer0.locations = @[@0, @1];
  [gradientLayer0 setStartPoint:CGPointMake(1, 1)];
  [gradientLayer0 setEndPoint:CGPointMake(0, 1)];
  return gradientLayer0;
}

- (IBAction)testPermission:(id)sender {
    //步数
    HKQuantityType *stepCountType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    //步数+跑步距离
    HKQuantityType *distance = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierDistanceWalkingRunning];
    //活动能量
    HKQuantityType *activeEnergyType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierActiveEnergyBurned];
    //睡眠分析
    HKCategoryType *sleepAnalysisType = [HKObjectType categoryTypeForIdentifier:HKCategoryTypeIdentifierSleepAnalysis];

    NSSet<HKSampleType *> *typesToShare = [NSSet setWithObjects:distance, activeEnergyType, sleepAnalysisType, nil];
    NSSet<HKObjectType *> *typesToRead = [NSSet setWithObjects:stepCountType, distance, activeEnergyType, sleepAnalysisType, nil];
    
    [[YdkPermissionManager requestHealthAuthorizationToShareTypes:typesToShare readTypes:typesToRead] subscribeNext:^(NSNumber *x) {
        [self fetchSteps];
    } error:^(NSError *error) {
        NSLog(@"error: %@", error.debugDescription);
    }];
}

- (IBAction)testLocation:(id)sender {
    [[YdkLocation requestLocation] subscribeNext:^(YdkLocationInfo *x) {
        NSLog(@"location info: %@", x);
    } error:^(NSError *error) {
        NSLog(@"location error: %@", error);
    }];
}

- (IBAction)testWebp:(id)sender {
    
    SDImageWebPCoder *webPCoder = [SDImageWebPCoder sharedCoder];
    [[SDImageCodersManager sharedManager] addCoder:webPCoder];
    
    UIImageView *imageView = [UIImageView new];
    imageView.backgroundColor = [UIColor tk_randomColor];
    imageView.tag = 100;
    imageView.frame = CGRectMake(100, 200, 100, 100);
    [imageView sd_setImageWithURL:[NSURL URLWithString:@"http://lvanjian-test.oss-cn-hangzhou.aliyuncs.com/nutrition-plan/image/default/201908/428686558224384.jpg?w=160&h=160&x-oss-process=image/auto-orient,1/quality,q_100/format,webp"] completed:^(UIImage * _Nullable image, NSError * _Nullable error, SDImageCacheType cacheType, NSURL * _Nullable imageURL) {
        NSLog(@"error: %@", error);
    }];
    [self.view addSubview:imageView];
}

- (void)fetchSteps {
    HKQuantityType *stepType = [HKObjectType quantityTypeForIdentifier:HKQuantityTypeIdentifierStepCount];
    [self fetchSumOfSamplesTodayForType:stepType unit:[HKUnit countUnit] completion:^(double stepCount, NSError *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSLog(@"你的步数为：%.f",stepCount);
        });
    }];
}

- (void)fetchSumOfSamplesTodayForType:(HKQuantityType *)quantityType unit:(HKUnit *)unit completion:(void (^)(double, NSError *))completionHandler {
    NSPredicate *predicate = [self predicateForSamplesToday];
    HKStatisticsQuery *query = [[HKStatisticsQuery alloc] initWithQuantityType:quantityType quantitySamplePredicate:predicate options:HKStatisticsOptionCumulativeSum completionHandler:^(HKStatisticsQuery *query, HKStatistics *result, NSError *error) {
        HKQuantity *sum = [result sumQuantity];
        if (completionHandler) {
            double value = [sum doubleValueForUnit:unit];
            completionHandler(value, error);
        }
    }];
    HKHealthStore *healthstore = [[HKHealthStore alloc] init];
    [healthstore executeQuery:query];
}

- (NSPredicate *)predicateForSamplesToday {
    NSCalendar *calendar = [NSCalendar currentCalendar];
    NSDate *now = [NSDate date];
    NSDate *startDate = [calendar startOfDayForDate:now];
    NSDate *endDate = [calendar dateByAddingUnit:NSCalendarUnitDay value:1 toDate:startDate options:0];
    return [HKQuery predicateForSamplesWithStartDate:startDate endDate:endDate options:HKQueryOptionStrictStartDate];
}

- (IBAction)trim:(id)sender {
    NSDictionary *config = @{@"type" : @(2),
                             @"style" : @{@"numColumns" : @3, @"isShowCamera" : @YES},
                             @"picture" : @{@"maxNum" : @5, @"isCrop" : @YES, @"cropScale" : @1 }
                             };
    YdkAlbumPickerConfig *ipConfig = [YdkAlbumPickerConfig yy_modelWithJSON:config];
    [[YdkAlbum presentImagePickerWithConfig:ipConfig sourceViewController:self] subscribeNext:^(NSArray *x) {
        DLog(@"Info: %@", x);
        id item = x.firstObject;
        if ([item isKindOfClass:[YdkVideoInfo class]]) {
            NSURL *videoURL = [NSURL fileURLWithPath:((YdkVideoInfo *)item).filePath];
            dispatch_async(dispatch_get_main_queue(), ^{
                AVURLAsset *asset = [AVURLAsset assetWithURL:videoURL];
                YdkVideoTrimmerViewController *videoTrimmerVC = [[YdkVideoTrimmerViewController alloc] init];
                videoTrimmerVC.asset = asset;
                [self presentViewController:videoTrimmerVC animated:YES completion:nil];
            });
        }
    } error:^(NSError * _Nullable error) {
        DLog(@"Error: %@", error);
    }];
}

- (IBAction)testIjkplayer {
    VideoViewController *video = [VideoViewController new];
    [self.navigationController pushViewController:video animated:YES];
}

@end

@implementation YdkNetworkInterceptor

+ (void)load {
    ydk_register_module(self);
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [YdkNetwork registerNetworkInterceptor:self];
    return YES;
}

- (YdkRequest *)requestWithIncompleteRequest:(YdkRequest *)request {
    [request.allHTTPHeaderFields setObject:@"346167614259200" forKey:@"userId"];
    [request.allHTTPHeaderFields setObject:@"1" forKey:@"devType"];
    [request.allHTTPHeaderFields setObject:@"lovelorn" forKey:@"tenantId"];
    [request.allHTTPHeaderFields setObject:@"1.4.0" forKey:@"appVersion"];
    [request.allHTTPHeaderFields setObject:@"SLAPP" forKey:@"ditchCode"];
    [request.allHTTPHeaderFields setObject:@"" forKey:@"devId"];
    [request.allHTTPHeaderFields setObject:@"192.168.30.188" forKey:@"ip"];
    [request.allHTTPHeaderFields setObject:@"MRp2889rlVgf1560738662237"  forKey:@"token"];
    return request;
}

@end

