//
//  SFKLogger.h
//  sense-sdk-ios-framework
//
//  Created by Marc-Henri Primault on 22.11.17.
//  Copyright © 2017 Sysmosoft. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 *  SENSE logger to handle logs with the server.
 */
@interface SFKLogger : NSObject

/**
 *  Generate or retrieve the unique instance of SFKLogger
 *
 *  @return The unique instance of SFKLogger
 */
+ (instancetype)instance;

/**
 *  Log the message to be retrieved by the server
 *
 *  @param message The message to log
 */
- (void)log:(NSString *)message;

@end
