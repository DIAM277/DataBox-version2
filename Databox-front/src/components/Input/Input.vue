<template>
    <label class="label28">
        <span class="span28">{{ label }}</span>
        <input class="inp28" :type="type" :placeholder="placeholder" :value="modelValue" @input="updateValue"
            :maxlength="maxLength" required>
    </label>
</template>

<script setup>

const props = defineProps({
    label: {
        type: String,
        default: '标签'
    },
    type: {
        type: String,
        default: 'text'
    },
    placeholder: {
        type: String,
        default: ''
    },
    modelValue: {
        type: [String, Number],
        default: ''
    },
    maxLength: {
        type: [String, Number],
        default: 150
    }
});

const emit = defineEmits(['update:modelValue']);

const updateValue = (event) => {
    emit('update:modelValue', event.target.value);
};
</script>

<style lang="scss" scoped>
.label28 {
    width: 100%;
    height: 80px;
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    cursor: text;
    margin-bottom: 15px;
}

.span28 {
    width: 100%;
    color: #000;
    font-size: 14px;
    margin-bottom: 6px;
    padding: 0 10px;
}

.inp28 {
    width: 100%;
    padding: 0 10px;
    height: 2px;
    border: 0;
    font-size: 14px;
    box-sizing: border-box;
    background-color: rgba(0, 0, 0, 0.1);
    transition: .3s;
}

.inp28:hover {
    background-color: rgba(50, 133, 255, 0.7);
}

.inp28:focus,
.inp28:valid {
    background-color: rgba(50, 133, 255, 0.2);
    border: 2px solid rgba(50, 133, 255, 0.7);
    border-radius: 4px;
    height: 42px;
    color: #000;
}
</style>