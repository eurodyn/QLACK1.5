package com.eurodyn.qlack2.be.forms.api.exception;

import com.eurodyn.qlack2.common.exception.QException;


public class QInvalidOperationException extends QException {

	private static final long serialVersionUID = 358488180820520850L;

	public QInvalidOperationException(String message) {
		super(message);
	}

}
