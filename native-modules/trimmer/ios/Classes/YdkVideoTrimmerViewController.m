//
//  YdkVideoTrimmerViewController.m
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import "YdkVideoTrimmerViewController.h"
#import "YdkVideoTrimmerView.h"

#import <ydk-toolkit/YdkToolkit.h>

@interface YdkVideoTrimmerViewController ()

@property (nonatomic, strong) YdkVideoTrimmerView *videoTrimmerView;

@end

@implementation YdkVideoTrimmerViewController
{
    UIButton *_cancelBtn;
    UIButton *_finishBtn;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor clearColor];
    CGRect frame = self.view.bounds;
    CGFloat height = frame.size.height;
    CGFloat width = frame.size.width;
    frame.size.height -= 64;
    YdkVideoTrimmerView *videoTrimmerView = [[YdkVideoTrimmerView alloc] initWithFrame:frame asset:_asset];
    [self.view addSubview:_videoTrimmerView = videoTrimmerView];
    
    UIButton *cancelBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [cancelBtn setTitle:@"取消" forState:UIControlStateNormal];
    [cancelBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    cancelBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    [cancelBtn addTarget:self action:@selector(cancelAction) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_cancelBtn = cancelBtn];
    
    UIButton *finishBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [finishBtn setTitle:@"确定" forState:UIControlStateNormal];
    [finishBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    finishBtn.titleLabel.font = [UIFont systemFontOfSize:15.0];
    [finishBtn addTarget:self action:@selector(confirmActon) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_finishBtn = finishBtn];
    
    CGFloat buttonH = 44;
    CGFloat buttonW = 60;
    
    _cancelBtn.frame = CGRectMake(0, height - buttonH, buttonW, buttonH);
    _finishBtn.frame = CGRectMake(width - buttonW, height - buttonH, buttonW, buttonH);
}

- (void)cancelAction {
    [_videoTrimmerView cancel];
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)confirmActon {
    [_videoTrimmerView trim:^(NSError *error, YdkVideoInfo *video) {
        [self dismissViewControllerAnimated:YES completion:nil];
        DLog(@"Result: %@", video);
        DLog(@"Error: %@", error.localizedDescription);
    }];
}

- (BOOL)prefersStatusBarHidden {
    return YES;
}

- (void)dealloc {
    DLog(@"%@ 销毁了.", NSStringFromClass(self.class));
}

@end
