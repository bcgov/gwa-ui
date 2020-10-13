export class ServiceAccount {
  id: string;
  key: string;
  secret: string;
  created_at: number;
  scope: string[] = [];
}
