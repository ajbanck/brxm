export class PickerService {

  constructor(DialogService, HstService) {
    'ngInject';

    this.DialogService = DialogService;
    this.HstService = HstService;
    this.treeData = { items: [] };
  }

  show(cfg) {
    return this.DialogService.show(angular.extend({
      clickOutsideToClose: true,
      templateUrl: 'channel/menu/picker/picker.html',
      controller: 'PickerCtrl',
      controllerAs: 'picker',
      bindToController: true,
    }, cfg));
  }

  getTree() {
    return this.treeData.items;
  }

  getInitialData(id, link) {
    return this.HstService.doGet(id, 'picker', link)
      .then((response) => angular.copy([response.data], this.treeData.items));
  }

  getData(item) {
    return this.HstService.doGet(item.id, 'picker').then((response) => {
      item.items = response.data.items;
    });
  }
}
