declare interface Module {
  routes?: { [routeName: string]: React.ComponentType<{}> };
}
