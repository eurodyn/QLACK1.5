/*
* Copyright 2014 EUROPEAN DYNAMICS SA <info@eurodyn.com>
*
* Licensed under the EUPL, Version 1.1 only (the "License").
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
* https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
package com.eurodyn.qlack2.fuse.chatim.api.exception;

import com.eurodyn.qlack2.common.exception.QException;

/**
 *
 * @author European Dynamids SA
 */
public class QChatIMException extends QException {
    private static final long serialVersionUID = -388144978690194303L;

//    public static enum CODES implements ExceptionCode {
//        ERR_CHA_0001,            // Generic JMS exception
//        ERR_CHA_0002,            // Error in provided arguments
//        ERR_CHA_0003             // JAXB error
//
//    }

    public QChatIMException(String message) {
        super(message);
    }


}