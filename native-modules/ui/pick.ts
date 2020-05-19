import { NativeModules } from 'react-native'

interface DateTimeConfig {
	pickerTitle?: string
	dateValue?: string //当前值
	minValue?: string
	maxValue?: string
	timeFormat?: string
}

interface LocationItem {
	label: string
	value: string
	children?: LocationItem[]
}

interface DataConfig {
	column?: number
	currentKey?: string
	pickerTitle?: string
	pickerData?: LocationItem[]
	pickerValue?: string[] //预选多列值
}

interface NativePickerRef {
	showLocationPicker(data: DataConfig): Promise<any>
	showDateTimePicker(data: DateTimeConfig): Promise<any>
	showSimplePicker(data: DataConfig): Promise<any>
}

export type NativePicker = NativePickerRef

export const NativePicker: NativePickerRef = NativeModules.NativePicker
