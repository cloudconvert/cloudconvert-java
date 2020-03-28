package com.cloudconvert.dto.request;

import com.cloudconvert.dto.Operation;

public abstract class TaskRequest extends Request {

    public abstract Operation getOperation();
}
