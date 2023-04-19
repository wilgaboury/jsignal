package com.github.wilgaboury.jsignal;

public class Ref<T>
{
    private T _ref;

    public Ref(T ref)
    {
        _ref = ref;
    }

    public T get()
    {
        return _ref;
    }

    public void set(T ref)
    {
        _ref = ref;
    }
}
