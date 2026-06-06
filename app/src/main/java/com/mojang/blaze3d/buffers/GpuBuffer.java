package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GpuBuffer implements AutoCloseable {
    private static final MemoryPool MEMORY_POOl = TracyClient.createMemoryPool("GPU Buffers");
    private final BufferType type;
    private final BufferUsage usage;
    private boolean closed;
    private boolean initialized = false;
    public final int handle;
    public int size;

    public GpuBuffer(BufferType p_374128_, BufferUsage p_374189_, int p_374082_) {
        this.type = p_374128_;
        this.size = p_374082_;
        this.usage = p_374189_;
        this.handle = GlStateManager._glGenBuffers();
    }

    public GpuBuffer(BufferType p_374296_, BufferUsage p_374056_, ByteBuffer p_374436_) {
        this(p_374296_, p_374056_, p_374436_.remaining());
        this.write(p_374436_, 0);
    }

    public void resize(int p_374325_) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        } else {
            if (this.initialized) {
                MEMORY_POOl.free((long)this.handle);
            }

            this.size = p_374325_;
            if (this.usage.writable) {
                this.initialized = false;
            } else {
                this.bind();
                GlStateManager._glBufferData(this.type.id, (long)p_374325_, this.usage.id);
                MEMORY_POOl.malloc((long)this.handle, p_374325_);
                this.initialized = true;
            }
        }
    }

    public void write(ByteBuffer p_374503_, int p_374343_) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        } else if (!this.usage.writable) {
            throw new IllegalStateException("Buffer is not writable");
        } else {
            int i = p_374503_.remaining();
            if (i + p_374343_ > this.size) {
                throw new IllegalArgumentException(
                    "Cannot write more data than this buffer can hold (attempting to write "
                        + i
                        + " bytes at offset "
                        + p_374343_
                        + " to "
                        + this.size
                        + " size buffer)"
                );
            } else {
                this.bind();
                if (this.initialized) {
                    GlStateManager._glBufferSubData(this.type.id, p_374343_, p_374503_);
                } else if (p_374343_ == 0 && i == this.size) {
                    GlStateManager._glBufferData(this.type.id, p_374503_, this.usage.id);
                    MEMORY_POOl.malloc((long)this.handle, this.size);
                    this.initialized = true;
                } else {
                    GlStateManager._glBufferData(this.type.id, (long)this.size, this.usage.id);
                    GlStateManager._glBufferSubData(this.type.id, p_374343_, p_374503_);
                    MEMORY_POOl.malloc((long)this.handle, this.size);
                    this.initialized = true;
                }
            }
        }
    }

    @Nullable
    public GpuBuffer.ReadView read() {
        return this.read(0, this.size);
    }

    @Nullable
    public GpuBuffer.ReadView read(int p_374066_, int p_374306_) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        } else if (!this.usage.readable) {
            throw new IllegalStateException("Buffer is not readable");
        } else if (p_374066_ + p_374306_ > this.size) {
            throw new IllegalArgumentException(
                "Cannot read more data than this buffer can hold (attempting to read "
                    + p_374306_
                    + " bytes at offset "
                    + p_374066_
                    + " from "
                    + this.size
                    + " size buffer)"
            );
        } else {
            this.bind();
            ByteBuffer bytebuffer = GlStateManager._glMapBufferRange(this.type.id, p_374066_, p_374306_, 1);
            return bytebuffer == null ? null : new GpuBuffer.ReadView(this.type.id, bytebuffer);
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            GlStateManager._glDeleteBuffers(this.handle);
            if (this.initialized) {
                MEMORY_POOl.free((long)this.handle);
            }
        }
    }

    public void bind() {
        GlStateManager._glBindBuffer(this.type.id, this.handle);
    }

    @OnlyIn(Dist.CLIENT)
    public static class ReadView implements AutoCloseable {
        private final int target;
        private final ByteBuffer data;

        protected ReadView(int p_374338_, ByteBuffer p_374301_) {
            this.target = p_374338_;
            this.data = p_374301_;
        }

        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            GlStateManager._glUnmapBuffer(this.target);
        }
    }
}
