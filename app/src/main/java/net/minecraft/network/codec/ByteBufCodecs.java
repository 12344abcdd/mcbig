package net.minecraft.network.codec;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ByteBufCodecs {
    int MAX_INITIAL_COLLECTION_SIZE = 65536;
    StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>() {
        public Boolean decode(ByteBuf p_320813_) {
            return p_320813_.readBoolean();
        }

        public void encode(ByteBuf p_319896_, Boolean p_320251_) {
            p_319896_.writeBoolean(p_320251_);
        }
    };
    StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>() {
        public Byte decode(ByteBuf p_320628_) {
            return p_320628_.readByte();
        }

        public void encode(ByteBuf p_320364_, Byte p_320618_) {
            p_320364_.writeByte(p_320618_);
        }
    };
    StreamCodec<ByteBuf, Float> ROTATION_BYTE = BYTE.map(Mth::unpackDegrees, Mth::packDegrees);
    StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>() {
        public Short decode(ByteBuf p_320513_) {
            return p_320513_.readShort();
        }

        public void encode(ByteBuf p_320028_, Short p_320388_) {
            p_320028_.writeShort(p_320388_);
        }
    };
    StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_320319_) {
            return p_320319_.readUnsignedShort();
        }

        public void encode(ByteBuf p_320669_, Integer p_320205_) {
            p_320669_.writeShort(p_320205_);
        }
    };
    StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_320253_) {
            return p_320253_.readInt();
        }

        public void encode(ByteBuf p_320753_, Integer p_330380_) {
            p_320753_.writeInt(p_330380_);
        }
    };
    StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_320759_) {
            return VarInt.read(p_320759_);
        }

        public void encode(ByteBuf p_320314_, Integer p_341414_) {
            VarInt.write(p_320314_, p_341414_);
        }
    };
    StreamCodec<ByteBuf, OptionalInt> OPTIONAL_VAR_INT = VAR_INT.map(
        p_378955_ -> p_378955_ == 0 ? OptionalInt.empty() : OptionalInt.of(p_378955_ - 1), p_378954_ -> p_378954_.isPresent() ? p_378954_.getAsInt() + 1 : 0
    );
    StreamCodec<ByteBuf, Long> LONG = new StreamCodec<ByteBuf, Long>() {
        public Long decode(ByteBuf p_320635_) {
            return p_320635_.readLong();
        }

        public void encode(ByteBuf p_320545_, Long p_341419_) {
            p_320545_.writeLong(p_341419_);
        }
    };
    StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>() {
        public Long decode(ByteBuf p_320259_) {
            return VarLong.read(p_320259_);
        }

        public void encode(ByteBuf p_320199_, Long p_376100_) {
            VarLong.write(p_320199_, p_376100_);
        }
    };
    StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>() {
        public Float decode(ByteBuf p_320599_) {
            return p_320599_.readFloat();
        }

        public void encode(ByteBuf p_320880_, Float p_376495_) {
            p_320880_.writeFloat(p_376495_);
        }
    };
    StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>() {
        public Double decode(ByteBuf p_319947_) {
            return p_319947_.readDouble();
        }

        public void encode(ByteBuf p_320370_, Double p_376267_) {
            p_320370_.writeDouble(p_376267_);
        }
    };
    StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>() {
        public byte[] decode(ByteBuf p_332176_) {
            return FriendlyByteBuf.readByteArray(p_332176_);
        }

        public void encode(ByteBuf p_331068_, byte[] p_376625_) {
            FriendlyByteBuf.writeByteArray(p_331068_, p_376625_);
        }
    };
    StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
    StreamCodec<ByteBuf, Tag> TAG = tagCodec(() -> NbtAccounter.create(2097152L));
    StreamCodec<ByteBuf, Tag> TRUSTED_TAG = tagCodec(NbtAccounter::unlimitedHeap);
    StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = compoundTagCodec(() -> NbtAccounter.create(2097152L));
    StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = compoundTagCodec(NbtAccounter::unlimitedHeap);
    StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>() {
        public Optional<CompoundTag> decode(ByteBuf p_319897_) {
            return Optional.ofNullable(FriendlyByteBuf.readNbt(p_319897_));
        }

        public void encode(ByteBuf p_320441_, Optional<CompoundTag> p_376225_) {
            FriendlyByteBuf.writeNbt(p_320441_, p_376225_.orElse(null));
        }
    };
    StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<ByteBuf, Vector3f>() {
        public Vector3f decode(ByteBuf p_324083_) {
            return FriendlyByteBuf.readVector3f(p_324083_);
        }

        public void encode(ByteBuf p_324192_, Vector3f p_376783_) {
            FriendlyByteBuf.writeVector3f(p_324192_, p_376783_);
        }
    };
    StreamCodec<ByteBuf, Quaternionf> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionf>() {
        public Quaternionf decode(ByteBuf p_324595_) {
            return FriendlyByteBuf.readQuaternion(p_324595_);
        }

        public void encode(ByteBuf p_324147_, Quaternionf p_376304_) {
            FriendlyByteBuf.writeQuaternion(p_324147_, p_376304_);
        }
    };
    StreamCodec<ByteBuf, Integer> CONTAINER_ID = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_324220_) {
            return FriendlyByteBuf.readContainerId(p_324220_);
        }

        public void encode(ByteBuf p_323874_, Integer p_376851_) {
            FriendlyByteBuf.writeContainerId(p_323874_, p_376851_);
        }
    };
    StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>() {
        private static final int MAX_PROPERTY_NAME_LENGTH = 64;
        private static final int MAX_PROPERTY_VALUE_LENGTH = 32767;
        private static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
        private static final int MAX_PROPERTIES = 16;

        public PropertyMap decode(ByteBuf p_360388_) {
            int i = ByteBufCodecs.readCount(p_360388_, 16);
            PropertyMap propertymap = new PropertyMap();

            for (int j = 0; j < i; j++) {
                String s = Utf8String.read(p_360388_, 64);
                String s1 = Utf8String.read(p_360388_, 32767);
                String s2 = FriendlyByteBuf.readNullable(p_360388_, p_376202_ -> Utf8String.read(p_376202_, 1024));
                Property property = new Property(s, s1, s2);
                propertymap.put(property.name(), property);
            }

            return propertymap;
        }

        public void encode(ByteBuf p_361922_, PropertyMap p_376665_) {
            ByteBufCodecs.writeCount(p_361922_, p_376665_.size(), 16);

            for (Property property : p_376665_.values()) {
                Utf8String.write(p_361922_, property.name(), 64);
                Utf8String.write(p_361922_, property.value(), 32767);
                FriendlyByteBuf.writeNullable(p_361922_, property.signature(), (p_376806_, p_376619_) -> Utf8String.write(p_376806_, p_376619_, 1024));
            }
        }
    };
    StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = new StreamCodec<ByteBuf, GameProfile>() {
        public GameProfile decode(ByteBuf p_376714_) {
            UUID uuid = UUIDUtil.STREAM_CODEC.decode(p_376714_);
            String s = Utf8String.read(p_376714_, 16);
            GameProfile gameprofile = new GameProfile(uuid, s);
            gameprofile.getProperties().putAll(ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(p_376714_));
            return gameprofile;
        }

        public void encode(ByteBuf p_376751_, GameProfile p_376462_) {
            UUIDUtil.STREAM_CODEC.encode(p_376751_, p_376462_.getId());
            Utf8String.write(p_376751_, p_376462_.getName(), 16);
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(p_376751_, p_376462_.getProperties());
        }
    };

    static StreamCodec<ByteBuf, byte[]> byteArray(final int p_324182_) {
        return new StreamCodec<ByteBuf, byte[]>() {
            public byte[] decode(ByteBuf p_320167_) {
                return FriendlyByteBuf.readByteArray(p_320167_, p_324182_);
            }

            public void encode(ByteBuf p_320240_, byte[] p_341316_) {
                if (p_341316_.length > p_324182_) {
                    throw new EncoderException("ByteArray with size " + p_341316_.length + " is bigger than allowed " + p_324182_);
                } else {
                    FriendlyByteBuf.writeByteArray(p_320240_, p_341316_);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, String> stringUtf8(final int p_320693_) {
        return new StreamCodec<ByteBuf, String>() {
            public String decode(ByteBuf p_341393_) {
                return Utf8String.read(p_341393_, p_320693_);
            }

            public void encode(ByteBuf p_340857_, String p_376210_) {
                Utf8String.write(p_340857_, p_376210_, p_320693_);
            }
        };
    }

    static StreamCodec<ByteBuf, Tag> tagCodec(final Supplier<NbtAccounter> p_320506_) {
        return new StreamCodec<ByteBuf, Tag>() {
            public Tag decode(ByteBuf p_376272_) {
                Tag tag = FriendlyByteBuf.readNbt(p_376272_, p_320506_.get());
                if (tag == null) {
                    throw new DecoderException("Expected non-null compound tag");
                } else {
                    return tag;
                }
            }

            public void encode(ByteBuf p_376553_, Tag p_376240_) {
                if (p_376240_ == EndTag.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                } else {
                    FriendlyByteBuf.writeNbt(p_376553_, p_376240_);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> p_331128_) {
        return tagCodec(p_331128_).map(p_339405_ -> {
            if (p_339405_ instanceof CompoundTag) {
                return (CompoundTag)p_339405_;
            } else {
                throw new DecoderException("Not a compound tag: " + p_339405_);
            }
        }, p_330975_ -> (Tag)p_330975_);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> p_331105_) {
        return fromCodec(p_331105_, NbtAccounter::unlimitedHeap);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> p_320615_) {
        return fromCodec(p_320615_, () -> NbtAccounter.create(2097152L));
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> p_330943_, Supplier<NbtAccounter> p_330382_) {
        return tagCodec(p_330382_)
            .map(
                p_337514_ -> p_330943_.parse(NbtOps.INSTANCE, p_337514_)
                        .getOrThrow(p_339407_ -> new DecoderException("Failed to decode: " + p_339407_ + " " + p_337514_)),
                p_337516_ -> p_330943_.encodeStart(NbtOps.INSTANCE, (T)p_337516_)
                        .getOrThrow(p_339409_ -> new EncoderException("Failed to encode: " + p_339409_ + " " + p_337516_))
            );
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> p_331713_) {
        return fromCodecWithRegistries(p_331713_, NbtAccounter::unlimitedHeap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> p_323797_) {
        return fromCodecWithRegistries(p_323797_, () -> NbtAccounter.create(2097152L));
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> p_331571_, Supplier<NbtAccounter> p_331922_) {
        final StreamCodec<ByteBuf, Tag> streamcodec = tagCodec(p_331922_);
        return new StreamCodec<RegistryFriendlyByteBuf, T>() {
            public T decode(RegistryFriendlyByteBuf p_376470_) {
                Tag tag = streamcodec.decode(p_376470_);
                RegistryOps<Tag> registryops = p_376470_.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                return p_331571_.parse(registryops, tag).getOrThrow(p_376477_ -> new DecoderException("Failed to decode: " + p_376477_ + " " + tag));
            }

            public void encode(RegistryFriendlyByteBuf p_376123_, T p_376288_) {
                RegistryOps<Tag> registryops = p_376123_.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                Tag tag = p_331571_.encodeStart(registryops, p_376288_)
                    .getOrThrow(p_376098_ -> new EncoderException("Failed to encode: " + p_376098_ + " " + p_376288_));
                streamcodec.encode(p_376123_, tag);
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<B, V> p_320522_) {
        return new StreamCodec<B, Optional<V>>() {
            public Optional<V> decode(B p_331901_) {
                return p_331901_.readBoolean() ? Optional.of(p_320522_.decode(p_331901_)) : Optional.empty();
            }

            public void encode(B p_331539_, Optional<V> p_376508_) {
                if (p_376508_.isPresent()) {
                    p_331539_.writeBoolean(true);
                    p_320522_.encode(p_331539_, p_376508_.get());
                } else {
                    p_331539_.writeBoolean(false);
                }
            }
        };
    }

    static int readCount(ByteBuf p_331813_, int p_331668_) {
        int i = VarInt.read(p_331813_);
        if (i > p_331668_) {
            throw new DecoderException(i + " elements exceeded max size of: " + p_331668_);
        } else {
            return i;
        }
    }

    static void writeCount(ByteBuf p_330907_, int p_330535_, int p_331447_) {
        if (p_330535_ > p_331447_) {
            throw new EncoderException(p_330535_ + " elements exceeded max size of: " + p_331447_);
        } else {
            VarInt.write(p_330907_, p_330535_);
        }
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> p_320579_, StreamCodec<? super B, V> p_319970_) {
        return collection(p_320579_, p_319970_, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(
        final IntFunction<C> p_332198_, final StreamCodec<? super B, V> p_332183_, final int p_332173_
    ) {
        return new StreamCodec<B, C>() {
            public C decode(B p_332082_) {
                int i = ByteBufCodecs.readCount(p_332082_, p_332173_);
                C c = p_332198_.apply(Math.min(i, 65536));

                for (int j = 0; j < i; j++) {
                    c.add(p_332183_.decode(p_332082_));
                }

                return c;
            }

            public void encode(B p_331172_, C p_376347_) {
                ByteBufCodecs.writeCount(p_331172_, p_376347_.size(), p_332173_);

                for (V v : p_376347_) {
                    p_332183_.encode(p_331172_, v);
                }
            }
        };
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> p_319808_) {
        return p_319785_ -> collection(p_319808_, p_319785_);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return p_320272_ -> collection(ArrayList::new, p_320272_);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int p_330434_) {
        return p_329871_ -> collection(ArrayList::new, p_329871_, p_330434_);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
        IntFunction<? extends M> p_320265_, StreamCodec<? super B, K> p_320113_, StreamCodec<? super B, V> p_320275_
    ) {
        return map(p_320265_, p_320113_, p_320275_, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
        final IntFunction<? extends M> p_331325_, final StreamCodec<? super B, K> p_331975_, final StreamCodec<? super B, V> p_331254_, final int p_330938_
    ) {
        return new StreamCodec<B, M>() {
            public void encode(B p_341417_, M p_376771_) {
                ByteBufCodecs.writeCount(p_341417_, p_376771_.size(), p_330938_);
                p_376771_.forEach((p_375471_, p_375472_) -> {
                    p_331975_.encode(p_341417_, (K)p_375471_);
                    p_331254_.encode(p_341417_, (V)p_375472_);
                });
            }

            public M decode(B p_340809_) {
                int i = ByteBufCodecs.readCount(p_340809_, p_330938_);
                M m = (M)p_331325_.apply(Math.min(i, 65536));

                for (int j = 0; j < i; j++) {
                    K k = p_331975_.decode(p_340809_);
                    V v = p_331254_.decode(p_340809_);
                    m.put(k, v);
                }

                return m;
            }
        };
    }

    static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(final StreamCodec<? super B, L> p_331983_, final StreamCodec<? super B, R> p_332156_) {
        return new StreamCodec<B, Either<L, R>>() {
            public Either<L, R> decode(B p_363037_) {
                return p_363037_.readBoolean() ? Either.left(p_331983_.decode(p_363037_)) : Either.right(p_332156_.decode(p_363037_));
            }

            public void encode(B p_364013_, Either<L, R> p_376807_) {
                p_376807_.ifLeft(p_376606_ -> {
                    p_364013_.writeBoolean(true);
                    p_331983_.encode(p_364013_, (L)p_376606_);
                }).ifRight(p_376693_ -> {
                    p_364013_.writeBoolean(false);
                    p_332156_.encode(p_364013_, (R)p_376693_);
                });
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> p_320877_, final ToIntFunction<T> p_319985_) {
        return new StreamCodec<ByteBuf, T>() {
            public T decode(ByteBuf p_376474_) {
                int i = VarInt.read(p_376474_);
                return p_320877_.apply(i);
            }

            public void encode(ByteBuf p_376188_, T p_361657_) {
                int i = p_319985_.applyAsInt(p_361657_);
                VarInt.write(p_376188_, i);
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> p_319822_) {
        return idMapper(p_319822_::byIdOrThrow, p_319822_::getIdOrThrow);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(
        final ResourceKey<? extends Registry<T>> p_319942_, final Function<Registry<T>, IdMap<R>> p_320353_
    ) {
        return new StreamCodec<RegistryFriendlyByteBuf, R>() {
            private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf p_362297_) {
                return p_320353_.apply(p_362297_.registryAccess().lookupOrThrow(p_319942_));
            }

            public R decode(RegistryFriendlyByteBuf p_340887_) {
                int i = VarInt.read(p_340887_);
                return (R)this.getRegistryOrThrow(p_340887_).byIdOrThrow(i);
            }

            public void encode(RegistryFriendlyByteBuf p_341009_, R p_376503_) {
                int i = this.getRegistryOrThrow(p_341009_).getIdOrThrow(p_376503_);
                VarInt.write(p_341009_, i);
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> p_320404_) {
        return registry(p_320404_, p_332056_ -> p_332056_);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> p_320387_) {
        return registry(p_320387_, Registry::asHolderIdMap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(
        final ResourceKey<? extends Registry<T>> p_320391_, final StreamCodec<? super RegistryFriendlyByteBuf, T> p_320595_
    ) {
        return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>() {
            private static final int DIRECT_HOLDER_ID = 0;

            private IdMap<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf p_376392_) {
                return p_376392_.registryAccess().lookupOrThrow(p_320391_).asHolderIdMap();
            }

            public Holder<T> decode(RegistryFriendlyByteBuf p_363509_) {
                int i = VarInt.read(p_363509_);
                return i == 0 ? Holder.direct(p_320595_.decode(p_363509_)) : (Holder)this.getRegistryOrThrow(p_363509_).byIdOrThrow(i - 1);
            }

            public void encode(RegistryFriendlyByteBuf p_362258_, Holder<T> p_376475_) {
                switch (p_376475_.kind()) {
                    case REFERENCE:
                        int i = this.getRegistryOrThrow(p_362258_).getIdOrThrow(p_376475_);
                        VarInt.write(p_362258_, i + 1);
                        break;
                    case DIRECT:
                        VarInt.write(p_362258_, 0);
                        p_320595_.encode(p_362258_, p_376475_.value());
                }
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> p_332137_) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>() {
            private static final int NAMED_SET = -1;
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec = ByteBufCodecs.holderRegistry(p_332137_);

            public HolderSet<T> decode(RegistryFriendlyByteBuf p_376912_) {
                int i = VarInt.read(p_376912_) - 1;
                if (i == -1) {
                    Registry<T> registry = p_376912_.registryAccess().lookupOrThrow(p_332137_);
                    return registry.get(TagKey.create(p_332137_, ResourceLocation.STREAM_CODEC.decode(p_376912_))).orElseThrow();
                } else {
                    List<Holder<T>> list = new ArrayList<>(Math.min(i, 65536));

                    for (int j = 0; j < i; j++) {
                        list.add(this.holderCodec.decode(p_376912_));
                    }

                    return HolderSet.direct(list);
                }
            }

            public void encode(RegistryFriendlyByteBuf p_376382_, HolderSet<T> p_376430_) {
                Optional<TagKey<T>> optional = p_376430_.unwrapKey();
                if (optional.isPresent()) {
                    VarInt.write(p_376382_, 0);
                    ResourceLocation.STREAM_CODEC.encode(p_376382_, optional.get().location());
                } else {
                    VarInt.write(p_376382_, p_376430_.size() + 1);

                    for (Holder<T> holder : p_376430_) {
                        this.holderCodec.encode(p_376382_, holder);
                    }
                }
            }
        };
    }
}
