package ai.utils;

import ai.utils.mock.MockData;
import lombok.Setter;

import java.util.function.Supplier;

public abstract class Mock {

    @Setter
    protected boolean enabled;

    protected Supplier<?> wrapper;

    public abstract <T> T mock(MockData mockData);

    public <T> Mock wrapper(Supplier<T> wrapper) {
        this.wrapper = wrapper;
        return this;
    }
}
