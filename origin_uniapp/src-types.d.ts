/// <reference types="@dcloudio/types" />

// 为 TS 识别 .vue 文件提供声明，防止 "Could not find declaration file" 报错
declare module '*.vue' {
	import type { DefineComponent } from 'vue';
	const component: DefineComponent<{}, {}, any>;
	export default component;
}

// uni-app 的全局变量声明
declare const uni: Uni;
declare const wx: WechatMiniprogram.Wx;

// 解决在非 HBuilderX 环境（如 VSCode）下找不到 '@dcloudio/uni-app' 模块的问题
declare module '@dcloudio/uni-app' {
	export function onLaunch(callback: (options?: any) => void): void;
	export function onLoad(callback: (query?: any) => void): void;
	export function onShow(callback: () => void): void;
	export function onHide(callback: () => void): void;
	export function onUnload(callback: () => void): void;
	export function onPullDownRefresh(callback: () => void): void;
	export function onReachBottom(callback: () => void): void;
	export function onPageScroll(callback: (options: { scrollTop: number }) => void): void;
	export function onShareAppMessage(callback: (options: any) => any): void;
	export function onResize(callback: (options: any) => void): void;
	export function onTabItemTap(callback: (options: any) => void): void;
}
