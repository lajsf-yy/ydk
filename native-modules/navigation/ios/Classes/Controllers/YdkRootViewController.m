//
//  LLRootViewController.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "YdkRootViewController.h"

#import <React/RCTConvert.h>
#import "YdkReactView.h"
#import "UIViewController+LayoutProtocol.h"
#import "YdkNavigationOptions.h"

#import <ydk-toolkit/YdkToolkit.h>

@interface YdkRootViewController ()

@property (readwrite) BOOL renderReady;

@end

@implementation YdkRootViewController
{
    UIView *_reactView;
}

 - (instancetype)initWithLayoutInfo:(YdkLayoutInfo *)layoutInfo creator:(id<YdkRootViewCreator>)creator eventEmitter:(YdkEventEmitter *)eventEmitter; {
    self = [super initWithLayoutInfo:layoutInfo creator:creator eventEmitter:eventEmitter];
    //  self.navigationController.delegate = self;
    return self;
}

- (BOOL)hidesBottomBarWhenPushed {
    if (self.tabBarController && self.navigationController && self.navigationController.viewControllers[0] == self) return NO;
    return YES;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    [self mergeOptions:self.options];
    [self.eventEmitter sendComponentDidAppear:self.layoutInfo.componentId componentName:self.layoutInfo.name];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    if (self.options && self.options.popGesture == NO) {
        // 当前页面禁掉侧滑时，当跳转到上级或下级页面时清除掉，交给页面设置侧滑手势
        [self mergeOptions:nil];
    }
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [self.eventEmitter sendComponentDidDisappear:self.layoutInfo.componentId componentName:self.layoutInfo.name];
}

- (void)setResult:(NSString *)componentId data:(id)data {
    [self.eventEmitter sendComponentReceiveResult:self.layoutInfo.componentId data:data];
}

- (void)renderTreeAndWait:(BOOL)wait perform:(YdkReactViewReadyCompletionBlock)readyBlock {
    __block YdkReactViewReadyCompletionBlock readyBlockCopy = readyBlock;
    NSMutableDictionary *layout = [NSMutableDictionary dictionary];
    if (self.layoutInfo.props) {
        [layout addEntriesFromDictionary:self.layoutInfo.props];
    }
    layout[@"name"] = self.layoutInfo.name;
    layout[@"componentId"] = self.layoutInfo.componentId;
    
    self.renderReady = NO;
    __weak __typeof(&*self) weakSelf = self;
    UIView *reactView = [self.creator createRootView:self.layoutInfo.name initialProperties:layout availableSize:self.view.size reactViewReadyBlock:^{
        __strong __typeof(&*weakSelf) strongSelf = weakSelf;
        strongSelf.renderReady = YES;
        if (readyBlockCopy) {
            readyBlockCopy();
            readyBlockCopy = nil;
        }
    }];
    if (_reactView) {
        [_reactView removeFromSuperview];
        _reactView = nil;
    }
    [self.view addSubview:_reactView = reactView];
    CGFloat bottom = 0;
    if (!self.hidesBottomBarWhenPushed) {
        bottom = TK_IS_iPhoneX_Series ? -34 : 0;
    }
    reactView.translatesAutoresizingMaskIntoConstraints = NO;
    NSMutableArray *constraints = [NSMutableArray array];
    [constraints addObject:[NSLayoutConstraint constraintWithItem:reactView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeTop multiplier:1.f constant:0.f]];
    [constraints addObject:[NSLayoutConstraint constraintWithItem:reactView attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeLeft multiplier:1.f constant:0.f]];
    [constraints addObject:[NSLayoutConstraint constraintWithItem:reactView attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeRight multiplier:1.f constant:0.f]];
    [constraints addObject:[NSLayoutConstraint constraintWithItem:reactView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self.view attribute:NSLayoutAttributeBottom multiplier:1.f constant:bottom]];
    [self.view addConstraints:constraints];
}

@end
