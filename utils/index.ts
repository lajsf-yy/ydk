export const camelCase = (value: string) => value.replace(/-([a-z])/g, g => g[1].toUpperCase());
export function shallowEqual(a: object, b: object) {
  if (a === b) {
    return true;
  }
  let keysA = Object.keys(a);
  let keysB = Object.keys(b);
  if (keysA.length !== keysB.length) {
    return false;
  }
  let hasDiff = keysA.some(key => {
    if (a[key] !== b[key]) {
      return true;
    }

    return false;
  });

  return !hasDiff;
}
