package com.zfj.android.sample;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;


import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by zfj_ on 2017/5/20.
 */

public class CountingRequestBody extends RequestBody {
    protected RequestBody delegate;
    private Listener listener;
    private CountingSink countingSink;

    public CountingRequestBody(RequestBody delegate, Listener listener){
        this.delegate = delegate;
        this.listener = listener;
    }
    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {
        private  long byteWritten;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            byteWritten += byteCount;
            listener.onRequestProgress(byteWritten, contentLength());
        }
    }

    public interface Listener{
        void onRequestProgress(long byteWrited, long contentLength);
    }
}
