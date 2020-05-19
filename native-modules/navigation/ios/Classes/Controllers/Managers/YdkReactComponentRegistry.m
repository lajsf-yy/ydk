//
//  YdkReactComponentRegistry.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkReactComponentRegistry.h"


@interface YdkReactComponentRegistry () {
  id<YdkRootViewCreator> _creator;
  NSMapTable* _componentStore;
}

@end

@implementation YdkReactComponentRegistry

- (instancetype)initWithCreator:(id<YdkRootViewCreator>)creator {
  self = [super init];
  _creator = creator;
  _componentStore = [NSMapTable new];
  return self;
}

- (YdkReactView *)createComponentIfNotExists:(YdkComponentOptions *)component parentComponentId:(NSString *)parentComponentId reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock  {
  NSMutableDictionary* parentComponentDict = [self componentsForParentId:parentComponentId];
  
  YdkReactView* reactView = [parentComponentDict objectForKey:component.componentId];
  if (!reactView) {
    reactView = (YdkReactView *)[_creator createRootViewFromComponentOptions:component reactViewReadyBlock:reactViewReadyBlock];
    [parentComponentDict setObject:reactView forKey:component.componentId];
  } else if (reactViewReadyBlock) {
    reactViewReadyBlock();
  }
  
  return reactView;
}

- (NSMutableDictionary *)componentsForParentId:(NSString *)parentComponentId {
  if (![_componentStore objectForKey:parentComponentId]) {
    [_componentStore setObject:[NSMutableDictionary new] forKey:parentComponentId];;
  }
  
  return [_componentStore objectForKey:parentComponentId];;
}

- (void)clearComponentsForParentId:(NSString *)parentComponentId {
  [_componentStore removeObjectForKey:parentComponentId];;
}

- (void)removeComponent:(NSString *)componentId {
  if ([_componentStore objectForKey:componentId]) {
    [_componentStore removeObjectForKey:componentId];
  }
}

- (void)clear {
  [_componentStore removeAllObjects];
}


@end
