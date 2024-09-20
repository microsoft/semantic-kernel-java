// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.chatcompletion;

import com.microsoft.semantickernel.services.StreamingKernelContent;

public interface StreamingChatContent<T> extends StreamingKernelContent<T> {

    public String getId();
}
